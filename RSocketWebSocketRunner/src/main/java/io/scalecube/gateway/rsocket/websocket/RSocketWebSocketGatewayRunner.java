package io.scalecube.gateway.rsocket.websocket;

import io.scalecube.services.Microservices;
import io.scalecube.transport.Address;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class RSocketWebSocketGatewayRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(RSocketWebSocketGatewayRunner.class);

  private static final Address SEED_ADDRESS = Address.from("localhost:4801");

  public static void main(String[] args) throws InterruptedException {
    LOGGER.info("Starting Gateway...");

    Microservices seed = Microservices.builder()
        .seeds(SEED_ADDRESS)
        .startAwait();

    RSocketWebSocketGateway gateway = new RSocketWebSocketGateway(seed);
    final InetSocketAddress address = gateway.start();

    LOGGER.info("Gateway started on " + address);

    Runtime.getRuntime().addShutdownHook(new Thread(gateway::stop));

    Thread.currentThread().join();
  }

}
