package io.scalecube.gateway.benchmarks.rsocket.standalone;

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

public class StandaloneMicrobenchmarkState extends AbstractBenchmarkState<StandaloneMicrobenchmarkState> {

  private static final GatewayConfig gatewayConfig =
      GatewayConfig.builder(RSocketWebsocketGateway.class).build();

  private Microservices microservices;

  public StandaloneMicrobenchmarkState(BenchmarksSettings settings) {
    super(settings);
  }

  @Override
  protected void beforeAll() throws Exception {
    super.beforeAll();

    microservices =
        Microservices.builder()
            .services(new ExampleServiceImpl())
            .gateway(gatewayConfig)
            .startAwait();
  }

  @Override
  protected void afterAll() throws Exception {
    super.afterAll();
    if (microservices != null) {
      microservices.shutdown().block();
    }
  }

  public Mono<Client> createClient() {
    InetSocketAddress gatewayAddress = microservices.gatewayAddress(gatewayConfig.gatewayClass());

    return createClient(
        ClientSettings.builder()
            .host(gatewayAddress.getHostString())
            .port(gatewayAddress.getPort())
            .build());
  }
}
