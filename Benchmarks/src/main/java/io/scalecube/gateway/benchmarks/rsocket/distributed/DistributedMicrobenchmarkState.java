package io.scalecube.gateway.benchmarks.rsocket.distributed;

import io.scalecube.benchmarks.BenchmarksSettings;
import io.scalecube.gateway.benchmarks.AbstractBenchmarkState;
import io.scalecube.gateway.benchmarks.example.ExampleServiceImpl;
import io.scalecube.gateway.clientsdk.Client;
import io.scalecube.gateway.clientsdk.ClientSettings;
import io.scalecube.gateway.rsocket.websocket.RSocketWebsocketGateway;
import io.scalecube.services.Microservices;
import io.scalecube.services.gateway.GatewayConfig;
import java.net.InetSocketAddress;
import reactor.core.publisher.Mono;

public class DistributedMicrobenchmarkState extends AbstractBenchmarkState<DistributedMicrobenchmarkState> {

  private static final String GATEWAY_ALIAS_NAME = "rsws";

  private static final GatewayConfig gatewayConfig =
      GatewayConfig.builder(GATEWAY_ALIAS_NAME, RSocketWebsocketGateway.class).build();

  private Microservices services;
  private Microservices gateway;

  public DistributedMicrobenchmarkState(BenchmarksSettings settings) {
    super(settings);
  }

  @Override
  protected void beforeAll() throws Exception {
    super.beforeAll();

    services = Microservices.builder()
        .services(new ExampleServiceImpl())
        .startAwait();

    gateway =
        Microservices.builder()
            .seeds(services.discovery().address())
            .gateway(gatewayConfig)
            .startAwait();
  }

  @Override
  protected void afterAll() throws Exception {
    super.afterAll();
    if (services != null) {
      services.shutdown().block();
    }
    if (gateway != null) {
      gateway.shutdown().block();
    }
  }

  @Override
  public Mono<Client> createClient() {
    InetSocketAddress gatewayAddress =
        gateway.gatewayAddress(GATEWAY_ALIAS_NAME, gatewayConfig.gatewayClass());

    return createClient(
        ClientSettings.builder()
            .host(gatewayAddress.getHostString())
            .port(gatewayAddress.getPort())
            .build());
  }
}
