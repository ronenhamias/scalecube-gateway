package io.scalecube.gateway.rsocket.core;

import static io.scalecube.gateway.core.GatewayMessage.QUALIFIER_FIELD;
import static java.nio.charset.StandardCharsets.UTF_8;

import io.scalecube.gateway.core.GatewayMessage;
import io.scalecube.services.codec.jackson.JacksonCodec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.util.ReferenceCountUtil;
import io.rsocket.Payload;
import io.rsocket.util.ByteBufPayload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class RSocketGatewayMessageCodec {

  private static final Logger LOGGER = LoggerFactory.getLogger(RSocketGatewayMessageCodec.class);

  private JacksonCodec jacksonCodec;

  public RSocketGatewayMessageCodec() {
    //TODO: decouple RSocketGatewayMessageCodec from scalecube-services-jackson
    jacksonCodec = new JacksonCodec();
  }

  public GatewayMessage decode(Payload payload) {
    final GatewayMessage.Builder builder = GatewayMessage.builder();

    final ByteBuf data = payload.sliceData();

    if (data.isReadable()) {
      builder.data(data);
    }

    final ByteBuf metadata = payload.sliceMetadata();

    if (metadata.isReadable()) {
      try (ByteBufInputStream stream = new ByteBufInputStream(metadata.slice())) {
        final Map<String, String> metadataMap = jacksonCodec.decode(stream);
        builder.qualifier(metadataMap.get(QUALIFIER_FIELD));
      } catch (Throwable ex) {
        LOGGER.error("Failed to decode metadata: {}, cause: {}", metadata.toString(UTF_8), ex);
        throw new GatewayException("Failed to decode metadata", ex);
      } finally {
        ReferenceCountUtil.safeRelease(metadata);
      }
    }

    return builder.build();
  }

  public Payload encode(GatewayMessage gatewayMessage) {
    ByteBuf metadata = ByteBufAllocator.DEFAULT.buffer();
    try {
      Map<String, String> metadataMap = new HashMap<>();
      metadataMap.put(QUALIFIER_FIELD, gatewayMessage.qualifier());
      jacksonCodec.encode(new ByteBufOutputStream(metadata), metadataMap);
    } catch (Throwable ex) {
      ReferenceCountUtil.safeRelease(metadata);
      LOGGER.error("Failed to encode metadata: {}, cause: {}", gatewayMessage, ex);
      throw new GatewayException("Failed to encode metadata on message q=" + gatewayMessage.qualifier(), ex);
    }

    return ByteBufPayload.create(gatewayMessage.data(), metadata);
  }

}
