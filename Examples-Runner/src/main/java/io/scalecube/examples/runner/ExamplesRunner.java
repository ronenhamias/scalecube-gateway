package io.scalecube.examples.runner;

import io.scalecube.gateway.websocket.GreetingServiceImpl;
import io.scalecube.services.Microservices;
import io.scalecube.transport.Address;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runner for example services.
 */
public class ExamplesRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExamplesRunner.class);
  private static final String DECORATOR = "***********************************************************************";

  /**
   * Main method of runner for example services.
   * 
   * @param args - program arguments.
   * @throws InterruptedException - thrown if was interrupted.
   */
  public static void main(String[] args) throws InterruptedException {
    ExamplesConfig config = new ExamplesConfig();
    int servicePort = config.port().value(ExamplesConfig.EXAMPLES_SERVICES_PORT_DEFAULT);
    Address seedAddr = config.seed();
    LOGGER.info(DECORATOR);
    LOGGER.info("Starting Examples services at port {}. Seed address: {}", servicePort, seedAddr);
    LOGGER.info(DECORATOR);
    Microservices service = Microservices.builder()
        .seeds(seedAddr)
        .servicePort(servicePort)
        .services(new GreetingServiceImpl()).build().startAwait();
    Thread.currentThread().join();
  }
}
