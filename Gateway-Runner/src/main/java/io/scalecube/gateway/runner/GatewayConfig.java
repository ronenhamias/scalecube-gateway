package io.scalecube.gateway.runner;

import io.scalecube.config.ConfigRegistry;
import io.scalecube.config.ConfigRegistrySettings;
import io.scalecube.config.IntConfigProperty;
import io.scalecube.config.audit.Slf4JConfigEventListener;
import io.scalecube.config.source.ClassPathConfigSource;
import io.scalecube.config.source.SystemEnvironmentConfigSource;
import io.scalecube.config.source.SystemPropertiesConfigSource;

import java.util.regex.Pattern;

public class GatewayConfig {

  private static final String GATEWAY_PORT_KEY = "gateway.port";
  static final int GATEWAY_PORT_DEFAULT = 8080;
  private String SEED_PORT_KEY = "seed.port";
  static final int SEED_PORT_DEFAULT = 4800;

  private static final String JMX_MBEAN_NAME = "scalecube.gateway:name=ConfigRegistry";
  private static final Pattern CONFIG_FILENAME_PATTERN = Pattern.compile("config/(.*)config(.*)?\\.properties");
  private static final int RELOAD_INTERVAL_SEC = 300;

  private final ConfigRegistry cfg;

  GatewayConfig() {
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
   * Listening port for gateway.
   * 
   * @return listening port.
   */
  IntConfigProperty port() {
    return cfg.intProperty(GATEWAY_PORT_KEY);
  }

  /**
   * Port for listening to cluster. Thi must be used by services as seed port.
   * 
   * @return Seed address if specified or null.
   */
  IntConfigProperty seedPort() {
    return cfg.intProperty(SEED_PORT_KEY);
  }
}
