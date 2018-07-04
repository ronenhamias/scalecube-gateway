package io.scalecube.gateway.rsocket.websocket;

import io.scalecube.gateway.core.GatewayMessageCodec;
import io.scalecube.services.Microservices;

import io.rsocket.RSocketFactory;
import io.rsocket.SocketAcceptor;
import io.rsocket.transport.netty.server.NettyContextCloseable;
import io.rsocket.transport.netty.server.WebsocketServerTransport;
import io.rsocket.util.ByteBufPayload;

import java.net.InetSocketAddress;

/**
 * Gateway implementation using RSocket protocol over WebSocket transport.
 *
 * @see io.rsocket.RSocket
 */
public class RSocketWebSocketGateway {

  private static final int DEFAULT_PORT = 8080;

  private InetSocketAddress address;
  private SocketAcceptor socketAcceptor;
  private NettyContextCloseable server;

  /**
   * Creates instance of gateway is listening on {@value DEFAULT_PORT} port.
   *
   * @param microservices instance of {@link Microservices}
   */
  public RSocketWebSocketGateway(Microservices microservices) {
    this(microservices, DEFAULT_PORT);
  }

  /**
   * Creates instance of gateway is listening on port which is passed as input parameter.
   *
   * @param microservices instance of {@link Microservices}
   * @param port gateway port
   */
  public RSocketWebSocketGateway(Microservices microservices, int port) {
    this(microservices, new InetSocketAddress(port));
  }

  /**
   * Creates instance of gateway listening on {@link InetSocketAddress} which is passed as input parameter.
   *
   * @param microservices instance of {@link Microservices}
   * @param inetSocketAddress gateway IP socket address
   */
  public RSocketWebSocketGateway(Microservices microservices, InetSocketAddress inetSocketAddress) {
    address = inetSocketAddress;
    socketAcceptor = new RSocketGatewayAcceptor(new GatewayMessageCodec(), microservices.call().create());
  }

  /**
   * Starts the gateway on configured IP socket address.
   *
   * @return IP socket address on which gateway is listening to requests
   */
  public InetSocketAddress start() {
    WebsocketServerTransport transport = WebsocketServerTransport.create(address.getHostName(), address.getPort());

    server = RSocketFactory.receive()
        .frameDecoder(frame -> ByteBufPayload.create(frame.sliceData().retain(), frame.sliceMetadata().retain()))
        .acceptor(socketAcceptor)
        .transport(transport)
        .start()
        .block();

    return address;
  }

  /**
   * Stops the gateway.
   */
  public void stop() {
    if (server != null) {
      server.dispose();
    }
  }

  public InetSocketAddress address() {
    return address;
  }

}
