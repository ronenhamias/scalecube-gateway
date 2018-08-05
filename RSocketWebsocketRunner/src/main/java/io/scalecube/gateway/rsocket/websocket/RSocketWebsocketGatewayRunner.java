package io.scalecube.gateway.rsocket.websocket;

import io.scalecube.app.decoration.Logo;
import io.scalecube.app.packages.PackageInfo;
import io.scalecube.config.ConfigRegistry;
import io.scalecube.gateway.config.GatewayConfigRegistry;
import io.scalecube.services.Microservices;
import io.scalecube.transport.Address;

import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricRegistry;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RSocketWebsocketGatewayRunner {

  private static final String SEEDS = "SEEDS";
  private static final List<String> DEFAULT_SEEDS = Collections.singletonList("localhost:4802");
  public static final String REPORTER_PATH = "reports/gw/metrics";

  public static void main(String[] args) throws InterruptedException {
    final ConfigRegistry configRegistry = GatewayConfigRegistry.configRegistry();

    final Address[] seeds = configRegistry.stringListValue(SEEDS, DEFAULT_SEEDS)
        .stream().map(Address::from).toArray(Address[]::new);

    MetricRegistry metrics = initMetricRegistry();

    Microservices seed = Microservices.builder()
        .seeds(seeds)
        .metrics(metrics)
        .startAwait();

    RSocketWebsocketServer gateway = new RSocketWebsocketServer(seed);

    gateway.start();

    Logo.from(new PackageInfo())
        .port(String.valueOf(seed.cluster().address().port()))
        .ip(seed.cluster().address().host())
        .draw();

    Runtime.getRuntime().addShutdownHook(new Thread(gateway::stop));

    Thread.currentThread().join();
  }

  private static MetricRegistry initMetricRegistry() {
    MetricRegistry metrics = new MetricRegistry();
    File reporterDir = new File(REPORTER_PATH);
    if (!reporterDir.exists()) {
      // noinspection ResultOfMethodCallIgnored
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
