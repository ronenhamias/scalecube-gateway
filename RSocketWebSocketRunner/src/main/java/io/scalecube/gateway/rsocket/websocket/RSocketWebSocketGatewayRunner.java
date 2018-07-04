package io.scalecube.gateway.rsocket.websocket;

import io.scalecube.services.Microservices;
import io.scalecube.transport.Address;

public class RSocketWebSocketGatewayRunner {

  private static final Address SEED_ADDRESS = Address.from("localhost:4801");

  public static void main(String[] args) throws InterruptedException {
    Microservices seed = Microservices.builder()
        .seeds(SEED_ADDRESS)
        .startAwait();

    RSocketWebSocketGateway gateway = new RSocketWebSocketGateway(seed);

    gateway.start();

    Runtime.getRuntime().addShutdownHook(new Thread(gateway::stop));

    Thread.currentThread().join();
  }

}
