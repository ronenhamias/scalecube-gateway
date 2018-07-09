package io.scalecube.gateway.websocket;

import io.scalecube.gateway.core.GatewayMessage;
import io.scalecube.gateway.core.GatewayMessageCodec;
import io.scalecube.gateway.core.Signal;
import io.scalecube.services.ServiceCall;
import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.exceptions.BadRequestException;
import io.scalecube.services.exceptions.ExceptionProcessor;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.ReferenceCountUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

public final class WebsocketAcceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsocketAcceptor.class);

  private final ServiceCall serviceCall;

  private final GatewayMessageCodec gatewayMessageCodec = new GatewayMessageCodec();

  public WebsocketAcceptor(ServiceCall serviceCall) {
    this.serviceCall = serviceCall;
  }

  /**
   * Connect handler method.
   *
   * @param session websocket session.
   * @return mono void.
   */
  public Mono<Void> onConnect(WebsocketSession session) {
    LOGGER.info("Session connected: " + session);

    Mono<Void> voidMono = session.send(session.receive()
        .flatMap(frame -> Flux.<GatewayMessage>create(sink -> {
          Long sid = null;
          try {
            GatewayMessage gatewayRequest = toMessage(frame);
            Long streamId = sid = gatewayRequest.streamId();

            // check message contains sid
            if (streamId == null) {
              LOGGER.error("Invalid gateway request: {}, " +
                  "sid is missing for session: {}", gatewayRequest, session);
              throw new BadRequestException("sid is missing");
            }

            // check session contains sid for CANCEL operation
            if (gatewayRequest.hasSignal(Signal.CANCEL)) {
              if (!session.dispose(streamId)) {
                LOGGER.error("CANCEL failed for gateway request: {}, " +
                    "sid={} is not contained in session: {}", gatewayRequest, streamId, session);
                throw new BadRequestException("sid=" + streamId + " is not contained in session");
              }
              sink.next(GatewayMessage.builder().streamId(streamId).signal(Signal.CANCEL).build());
              sink.complete();
              return;
            }

            // check session not yet contain sid
            if (session.containsSid(streamId)) {
              LOGGER.error("Failed gateway request: {}, " +
                  "sid={} is already registered on session: {}", gatewayRequest, session);
              throw new BadRequestException("sid=" + streamId + " is already registered on session");
            }

            if (gatewayRequest.qualifier() == null) {
              LOGGER.error("Failed gateway request: {}, q is missing for session: {}", gatewayRequest, session);
              throw new BadRequestException("q is missing");
            }

            AtomicBoolean receivedErrorMessage = new AtomicBoolean(false);

            ServiceMessage serviceRequest = GatewayMessage.toServiceMessage(gatewayRequest);
            Flux<ServiceMessage> serviceStream = serviceCall.requestMany(serviceRequest);

            if (gatewayRequest.inactivity() != null) {
              serviceStream = serviceStream.timeout(Duration.ofMillis(gatewayRequest.inactivity()));
            }

            Disposable disposable = serviceStream
                .map(serviceResponse -> {
                  GatewayMessage.Builder gatewayResponse = GatewayMessage.from(serviceResponse).streamId(streamId);
                  if (ExceptionProcessor.isError(serviceResponse)) {
                    receivedErrorMessage.set(true);
                    gatewayResponse.signal(Signal.ERROR);
                  }
                  return gatewayResponse.build();
                })
                .concatWith(Flux.defer(() -> receivedErrorMessage.get()
                    ? Mono.empty()
                    : Mono.just(GatewayMessage.builder().streamId(streamId).signal(Signal.COMPLETE).build())))
                .onErrorResume(t -> Mono.just(toErrorMessage(t, streamId)))
                .doOnTerminate(() -> session.dispose(streamId))
                .subscribe(sink::next, sink::error, sink::complete);

            session.register(sid, disposable);
          } catch (Throwable ex) {
            ReferenceCountUtil.safeRelease(frame);
            sink.next(toErrorMessage(ex, sid));
            sink.complete();
          }
        }))
        .flatMap(this::toByteBuf)
        .doOnError(ex -> LOGGER.error("Unhandled exception occured: {}, " +
            "session: {} will be closed", ex, session, ex)));

    session.onClose(() -> LOGGER.info("Session disconnected: " + session));
    return voidMono.then();
  }

  /**
   * Disconnect handler method.
   *
   * @param session websocket session.
   * @return mono void.
   */
  public Mono<Void> onDisconnect(WebsocketSession session) {
    LOGGER.info("Session disconnected: " + session);
    return Mono.empty();
  }

  private Mono<ByteBuf> toByteBuf(GatewayMessage message) {
    try {
      return Mono.just(gatewayMessageCodec.encode(message));
    } catch (Throwable ex) {
      ReferenceCountUtil.safeRelease(message.data());
      return Mono.empty();
    }
  }

  private GatewayMessage toMessage(WebSocketFrame frame) {
    try {
      return gatewayMessageCodec.decode(frame.content());
    } catch (Throwable ex) {
      ReferenceCountUtil.safeRelease(frame);
      throw new BadRequestException(ex.getMessage());
    }
  }

  private GatewayMessage toErrorMessage(Throwable th, Long streamId) {
    ServiceMessage serviceMessage = ExceptionProcessor.toMessage(th);
    return GatewayMessage.from(serviceMessage).streamId(streamId).signal(Signal.ERROR).build();
  }
}
