package io.scalecube.gateway.rsocket.websocket;

import io.scalecube.gateway.core.GatewayMessage;
import io.scalecube.gateway.rsocket.core.RSocketGatewayMessageCodec;
import io.scalecube.services.ServiceCall;
import io.scalecube.services.api.ServiceMessage;

import io.rsocket.AbstractRSocket;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.util.ByteBufPayload;

import org.reactivestreams.Publisher;

import java.util.function.Function;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class RSocketGatewayAcceptor implements SocketAcceptor {

  private RSocketGatewayMessageCodec codec;
  private ServiceCall serviceCall;

  public RSocketGatewayAcceptor(RSocketGatewayMessageCodec codec, ServiceCall serviceCall) {
    this.codec = codec;
    this.serviceCall = serviceCall;
  }

  @Override
  public Mono<RSocket> accept(ConnectionSetupPayload setup, RSocket sendingSocket) {
    return Mono.just(new GatewayRSocket());
  }

  private ServiceMessage toServiceMessage(Payload payload) {
    return codec.decode(payload).toServiceMessage();
  }

  private Payload toPayload(ServiceMessage serviceMessage) {
    final GatewayMessage gatewayMessage = GatewayMessage.toGatewayMessage(serviceMessage);
    return ByteBufPayload.create(codec.encode(gatewayMessage));
  }

  private class GatewayRSocket extends AbstractRSocket {

    private Function<ServiceMessage, Payload> toPayload = RSocketGatewayAcceptor.this::toPayload;
    private Function<Payload, ServiceMessage> toServiceMessage = RSocketGatewayAcceptor.this::toServiceMessage;

    @Override
    public Mono<Void> fireAndForget(Payload payload) {
      return serviceCall.oneWay(toServiceMessage(payload));
    }

    @Override
    public Mono<Payload> requestResponse(Payload payload) {
      return serviceCall.requestOne(toServiceMessage(payload)).map(toPayload);
    }

    @Override
    public Flux<Payload> requestStream(Payload payload) {
      return serviceCall.requestMany(toServiceMessage(payload)).map(toPayload);
    }

    @Override
    public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
      final Publisher<ServiceMessage> publisher = Flux.from(payloads).map(toServiceMessage);
      return serviceCall.requestBidirectional(publisher).map(toPayload);
    }

  }

}
