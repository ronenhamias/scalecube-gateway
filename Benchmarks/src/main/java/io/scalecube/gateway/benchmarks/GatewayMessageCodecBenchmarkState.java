package io.scalecube.gateway.benchmarks;

import io.scalecube.benchmarks.BenchmarksSettings;
import io.scalecube.benchmarks.BenchmarksState;
import io.scalecube.gateway.websocket.message.GatewayMessage;
import io.scalecube.gateway.websocket.message.GatewayMessageCodec;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class GatewayMessageCodecBenchmarkState extends BenchmarksState<GatewayMessageCodecBenchmarkState> {

  private final static String GW_MSG_PATTERN = "{" +
      "\"q\":\"%s\"," +
      "\"sig\":%d," +
      "\"sid\":%d," +
      "\"d\":%s" +
      "}";

  private GatewayMessageCodec gatewayMessageCodec;

  private final ObjectMapper objectMapper = objectMapper();
  private GatewayMessage gatewayMessage;
  private ByteBuf byteBufExample;

  public GatewayMessageCodecBenchmarkState(BenchmarksSettings settings) {
    super(settings);
  }

  @Override
  protected void beforeAll() throws JsonProcessingException {
    this.gatewayMessageCodec = new GatewayMessageCodec();
    this.gatewayMessage = generateGatewayMessage(generateByteBuf(generateExample()));
    this.byteBufExample = generateByteBuf(generateGatewayMessage(generateExample()));
  }

  public GatewayMessageCodec codec() {
    return gatewayMessageCodec;
  }

  public ByteBuf byteBufExample() {
    return byteBufExample.slice();
  }

  public GatewayMessage message() {
    return gatewayMessage;
  }

  private PlaceOrderRequest generateExample() {
    PlaceOrderRequest result = new PlaceOrderRequest();
    result.orderType = "Sell";
    result.side = "Sell";
    result.instanceId = UUID.randomUUID().toString();
    result.quantity = BigDecimal.valueOf(Long.MAX_VALUE);
    result.price = BigDecimal.valueOf(Long.MAX_VALUE);
    result.isClosePositionOrder = false;
    result.requestTimestamp = LocalDateTime.now();
    result.sourceIpAddress = "255.255.255.255";
    result.token =
        "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJUZW5hbnQxIiwic3ViIjoiMSIsIm5hbWUiOiJ0cmFkZXIxIn0.j9dCs63J4xtWfhctrXb5popLAl8ohSlMTJU3_vCrQHk";
    return result;
  }

  private GatewayMessage generateGatewayMessage(Object data) {
    return GatewayMessage.builder()
        .qualifier("io.scalecube.gateway.benchmarks/SomeBenchmarkService/benchmark")
        .streamId(Long.MAX_VALUE)
        .signal(9)
        .inactivity(Integer.MAX_VALUE)
        .data(data)
        .build();
  }

  private ByteBuf generateByteBuf(GatewayMessage msg) throws JsonProcessingException {
    ByteBuf bb = ByteBufAllocator.DEFAULT.buffer();
    String jsonData = objectMapper.writeValueAsString(msg.data());
    String data = String.format(GW_MSG_PATTERN, msg.qualifier(), msg.signal(), msg.streamId(), jsonData);
    bb.writeBytes(data.getBytes());
    System.out.println("generated ByteBuf: " + data);
    return bb;
  }

  private ByteBuf generateByteBuf(Object data) throws JsonProcessingException {
    ByteBuf bb = ByteBufAllocator.DEFAULT.buffer();
    String rawData = objectMapper.writeValueAsString(data);
    bb.writeBytes(rawData.getBytes());
    System.out.println("generated ByteBuf: " + rawData);
    return bb;
  }

  private ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
    mapper.registerModule(new JavaTimeModule());
    return mapper;
  }

  public static class PlaceOrderRequest {
    private String orderType;
    private String side;
    private String instanceId;
    private BigDecimal quantity;
    private BigDecimal price;
    private boolean isClosePositionOrder;
    private LocalDateTime requestTimestamp;
    private String token;
    private String sourceIpAddress;

    PlaceOrderRequest() {}

    @Override
    public String toString() {
      return "PlaceOrderRequest{" +
          "token='" + token + '\'' +
          ", sourceIpAddress='" + sourceIpAddress + '\'' +
          ", orderType='" + orderType + '\'' +
          ", side='" + side + '\'' +
          ", side='" + side + '\'' +
          ", instanceId='" + instanceId + '\'' +
          ", quantity=" + quantity +
          ", price=" + price +
          ", isClosePositionOrder=" + isClosePositionOrder +
          ", requestTimestamp=" + requestTimestamp +
          '}';
    }

  }
}
