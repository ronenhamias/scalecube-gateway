package io.scalecube.gateway.rsocket.websocket;

import io.scalecube.config.ConfigRegistry;
import io.scalecube.gateway.config.GatewayConfigRegistry;
import io.scalecube.services.Microservices;
import io.scalecube.transport.Address;

import java.util.Collections;
import java.util.List;

public class RSocketWebSocketGatewayRunner {

  private static final String SEEDS = "SEEDS";
  private static final List<String> DEFAULT_SEEDS = Collections.singletonList("localhost:4802");

  public static void main(String[] args) throws InterruptedException {
    final ConfigRegistry configRegistry = GatewayConfigRegistry.configRegistry();

    final Address[] seeds = configRegistry.stringListValue(SEEDS, DEFAULT_SEEDS)
        .stream().map(Address::from).toArray(Address[]::new);

    Microservices seed = Microservices.builder()
        .seeds(seeds)
        .startAwait();

    RSocketWebsocketGateway gateway = new RSocketWebsocketGateway(seed);

    gateway.start();

    Runtime.getRuntime().addShutdownHook(new Thread(gateway::stop));

    Thread.currentThread().join();
  }

}
