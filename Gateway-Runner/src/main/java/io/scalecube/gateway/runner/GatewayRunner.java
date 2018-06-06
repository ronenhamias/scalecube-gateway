package io.scalecube.gateway.runner;

import static io.scalecube.gateway.runner.GatewayConfig.GATEWAY_PORT_DEFAULT;
import static io.scalecube.gateway.runner.GatewayConfig.SEED_PORT_DEFAULT;

import io.scalecube.cluster.ClusterConfig;
import io.scalecube.gateway.websocket.WebSocketServer;
import io.scalecube.services.Microservices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Runs gateway server.
 */
public class GatewayRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(GatewayRunner.class);
  private static final String DECORATOR = "***********************************************************************";

  /**
   * Main method to run gateway server.
   *
   * @param args - program arguments.
   */
  public static void main(String[] args) throws InterruptedException {
    GatewayConfig config = new GatewayConfig();

    InetSocketAddress listenAddress = new InetSocketAddress(config.port().value(GATEWAY_PORT_DEFAULT));
    int seedPort = config.seedPort().value(SEED_PORT_DEFAULT);

    Microservices seed = Microservices.builder()
        .clusterConfig(ClusterConfig.builder().port(seedPort))
        .build()
        .startAwait();

    LOGGER.info(DECORATOR);
    LOGGER.info("Starting Websocket Gateway services at port {}. Seed listening to port: {}",
        listenAddress.getPort(), seedPort);
    LOGGER.info(DECORATOR);

    WebSocketServer server = new WebSocketServer(seed);
    server.start(listenAddress);

    Thread.currentThread().join();
  }
}
