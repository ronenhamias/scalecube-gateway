package io.scalecube.gateway.benchmarks.distributed.websocket;

import io.scalecube.gateway.benchmarks.SharedStreamScenario;
import io.scalecube.gateway.benchmarks.distributed.DistributedBenchmarkState;
import io.scalecube.gateway.clientsdk.Client;
import io.scalecube.gateway.clientsdk.ClientSettings;

public class DistributedSharedStreamBenchmark {

  /**
   * Main runner.
   *
   * @param args program arguments
   */
  public static void main(String[] args) {
    SharedStreamScenario.runWith(
        args,
        benchmarkSettings ->
            new DistributedBenchmarkState(
                benchmarkSettings,
                "ws",
                (address, loopResources) ->
                    Client.onWebsocket(
                        ClientSettings.builder()
                            .address(address)
                            .loopResources(loopResources)
                            .build())));
  }
}
