package io.scalecube.gateway.websocket;

import io.scalecube.config.ConfigRegistry;
import io.scalecube.gateway.config.GatewayConfigRegistry;
import io.scalecube.services.Microservices;
import io.scalecube.transport.Address;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Runs gateway server.
 */
public class WebsocketGatewayRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsocketGatewayRunner.class);
  private static final String DECORATOR = "***********************************************************************";

  /**
   * Main method to run gateway server.
   *
   * @param args - program arguments.
   */
  public static void main(String[] args) throws InterruptedException {
    ConfigRegistry configRegistry = GatewayConfigRegistry.configRegistry();

    WebsocketGatewayConfig config =
        configRegistry.objectValue("io.scalecube.gateway.websocket", WebsocketGatewayConfig.class, null);

    LOGGER.info(DECORATOR);
    LOGGER.info("Starting Websocket Gateway on " + config);
    LOGGER.info(DECORATOR);

    Address seedAddress = Address.from(config.getSeedAddress());
    int websocketPort = config.getWebsocketPort();

    InetSocketAddress listenAddress = new InetSocketAddress(websocketPort);

    Microservices seed = Microservices.builder()
        .seeds(seedAddress)
        .startAwait();

    WebSocketServer server = new WebSocketServer(seed);
    server.start(listenAddress);

    Thread.currentThread().join();
  }
}
