package io.scalecube.gateway.websocket;

import io.scalecube.config.ConfigRegistry;
import io.scalecube.gateway.config.GatewayConfigRegistry;
import io.scalecube.services.Microservices;
import io.scalecube.services.gateway.GatewayConfig;
import io.scalecube.transport.Address;

import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Runs gateway server.
 */
public class WebsocketGatewayRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsocketGatewayRunner.class);
  private static final String DECORATOR = "***********************************************************************";
  private static final String REPORTER_PATH = "reports/gw/metrics";

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

    MetricRegistry metrics = initMetricRegistry();

    Microservices seed = Microservices.builder()
        .seeds(seedAddress)
        .gateway(GatewayConfig.builder("ws", WebsocketGateway.class).port(websocketPort).build())
        .metrics(metrics)
        .startAwait();

    Thread.currentThread().join();
  }

  private static MetricRegistry initMetricRegistry() {
    MetricRegistry metrics = new MetricRegistry();
    File reporterDir = new File(REPORTER_PATH);
    if (!reporterDir.exists()) {
      //noinspection ResultOfMethodCallIgnored
      reporterDir.mkdirs();
    }
    CsvReporter csvReporter = CsvReporter.forRegistry(metrics)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .convertRatesTo(TimeUnit.SECONDS)
      .build(reporterDir);

    csvReporter.start(10, TimeUnit.SECONDS);
    return metrics;
  }

}
