package io.scalecube.gateway.benchmarks.rsocket.standalone;

import io.scalecube.benchmarks.BenchmarkSettings;
import io.scalecube.gateway.benchmarks.AbstractBenchmarkState;
import io.scalecube.gateway.benchmarks.BenchmarksServiceImpl;
import io.scalecube.gateway.clientsdk.Client;
import io.scalecube.gateway.clientsdk.ClientSettings;
import io.scalecube.gateway.rsocket.websocket.RSocketWebsocketGateway;
import io.scalecube.services.Microservices;
import io.scalecube.services.gateway.GatewayConfig;
import java.net.InetSocketAddress;
import reactor.core.publisher.Mono;

public class StandaloneMicrobenchmarkState
    extends AbstractBenchmarkState<StandaloneMicrobenchmarkState> {

  private static final String GATEWAY_ALIAS_NAME = "rsws";

  private static final GatewayConfig gatewayConfig =
      GatewayConfig.builder(GATEWAY_ALIAS_NAME, RSocketWebsocketGateway.class).build();

  private Microservices microservices;

  public StandaloneMicrobenchmarkState(BenchmarkSettings settings) {
    super(settings);
  }

  @Override
  protected void beforeAll() throws Exception {
    super.beforeAll();

    microservices =
        Microservices.builder()
            .services(new BenchmarksServiceImpl())
            .gateway(gatewayConfig)
            .metrics(registry())
            .startAwait();
  }

  @Override
  protected void afterAll() throws Exception {
    super.afterAll();
    if (microservices != null) {
      microservices.shutdown().block();
    }
  }

  /**
   * Factory function for {@link Client}.
   *
   * @return client
   */
  public Mono<Client> createClient() {
    InetSocketAddress gatewayAddress =
        microservices.gatewayAddress(GATEWAY_ALIAS_NAME, gatewayConfig.gatewayClass());

    return createClient(
        ClientSettings.builder()
            .host(gatewayAddress.getHostString())
            .port(gatewayAddress.getPort())
            .build());
  }
}
