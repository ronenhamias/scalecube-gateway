package io.scalecube.examples.runner;

import io.scalecube.config.ConfigRegistry;
import io.scalecube.config.ConfigRegistrySettings;
import io.scalecube.config.IntConfigProperty;
import io.scalecube.config.audit.Slf4JConfigEventListener;
import io.scalecube.config.source.ClassPathConfigSource;
import io.scalecube.config.source.SystemEnvironmentConfigSource;
import io.scalecube.config.source.SystemPropertiesConfigSource;
import io.scalecube.transport.Address;

import java.util.regex.Pattern;

/**
 * Config properties for example services.
 */
public class ExamplesConfig {

  private static final String JMX_MBEAN_NAME = "scalecube.services.examples:name=ConfigRegistry";
  private static final Pattern CONFIG_FILENAME_PATTERN = Pattern.compile("config/(.*)config(.*)?\\.properties");
  private static final int RELOAD_INTERVAL_SEC = 300;

  private static final String EXAMPLES_SERVICES_PORT_KEY = "service.port";
  static final int EXAMPLES_SERVICES_PORT_DEFAULT = 4810;

  private String EXAMPLES_SEED_HOST_KEY = "seed.host";
  private static final String SEED_HOST_DEFAULT = "localhost";

  private String EXAMPLES_SEED_PORT_KEY = "seed.port";
  private static final int SEED_PORT_DEFAULT = 4800;

  private final ConfigRegistry cfg;

  ExamplesConfig() {
    cfg = ConfigRegistry.create(ConfigRegistrySettings.builder()
        .addListener(new Slf4JConfigEventListener())
        .addLastSource("sys_prop", new SystemPropertiesConfigSource())
        .addLastSource("env_var", new SystemEnvironmentConfigSource())
        .addLastSource("cp",
            new ClassPathConfigSource(path -> CONFIG_FILENAME_PATTERN.matcher(path.getFileName().toString()).matches()))
        .jmxMBeanName(JMX_MBEAN_NAME)
        .reloadIntervalSec(RELOAD_INTERVAL_SEC)
        .build());
  }

  /**
   * Listening port for example services.
   * 
   * @return listening port.
   */
  IntConfigProperty port() {
    return cfg.intProperty(EXAMPLES_SERVICES_PORT_KEY);
  }

  /**
   * Seed for example services. If no seed provided - will return null.
   * 
   * @return Seed address if specified or null.
   */
  Address seed() {
    String seedHost = cfg.stringValue(EXAMPLES_SEED_HOST_KEY, SEED_HOST_DEFAULT);
    int seedPort = cfg.intValue(EXAMPLES_SEED_PORT_KEY, SEED_PORT_DEFAULT);

    if (seedHost != null && seedPort > 0) {
      return Address.create(seedHost, seedPort);
    }
    return null;
  }
}
