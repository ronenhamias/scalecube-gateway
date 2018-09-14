package io.scalecube.gateway.websocket.message;

import io.scalecube.services.api.ServiceMessage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GatewayMessage {

  public static final String QUALIFIER_FIELD = "q";
  public static final String STREAM_ID_FIELD = "sid";
  public static final String SIGNAL_FIELD = "sig";
  public static final String DATA_FIELD = "d";
  public static final String INACTIVITY_FIELD = "i";

  private static final String SERVICE_MESSAGE_HEADER_DATA_TYPE = "_type";
  private static final String SERVICE_MESSAGE_HEADER_DATA_FORMAT = "_data_format";

  private final Map<String, String> headers;
  private final Object data;

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Get a builder by pattern form given {@link GatewayMessage}.
   *
   * @param msg Message form where to copy field values.
   * @return builder with fields copied from given {@link GatewayMessage}
   */
  public static Builder from(GatewayMessage msg) {
    return new Builder().headers(msg.headers).data(msg.data);
  }

  /**
   * Get a builder by pattern form given {@link ServiceMessage}.
   *
   * @param serviceMessage ServiceMessage form where to copy field values.
   * @return builder with fields copied from given {@link ServiceMessage}
   */
  public static Builder from(ServiceMessage serviceMessage) {
    Builder builder = new Builder();
    if (serviceMessage.hasData()) {
      builder.data = serviceMessage.data();
    }
    serviceMessage
        .headers()
        .forEach(
            (key, value) -> {
              switch (key) {
                case SERVICE_MESSAGE_HEADER_DATA_FORMAT:
                case SERVICE_MESSAGE_HEADER_DATA_TYPE:
                  break;
                default:
                  builder.header(key, value);
              }
            });
    return builder;
  }

  private GatewayMessage(Builder builder) {
    this.data = builder.data;
    this.headers = Collections.unmodifiableMap(builder.headers);
  }

  public static GatewayMessage toGatewayMessage(ServiceMessage serviceMessage) {
    return from(serviceMessage).build();
  }

  public ServiceMessage toServiceMessage() {
    return toServiceMessage(this);
  }

  /**
   * {@link GatewayMessage} to {@link ServiceMessage} converter.
   *
   * @param gatewayMessage gateway message
   * @return service message
   */
  public static ServiceMessage toServiceMessage(GatewayMessage gatewayMessage) {
    ServiceMessage.Builder builder =
        ServiceMessage.builder().qualifier(gatewayMessage.qualifier()).data(gatewayMessage.data);
    gatewayMessage.headers.forEach(builder::header);
    return builder.build();
  }

  public String qualifier() {
    return headers.get(QUALIFIER_FIELD);
  }

  public Long streamId() {
    String value = headers.get(STREAM_ID_FIELD);
    return value != null ? Long.parseLong(value) : null;
  }

  public Integer signal() {
    String value = headers.get(SIGNAL_FIELD);
    return value != null ? Integer.valueOf(value) : null;
  }

  public <T> T data() {
    // noinspection unchecked
    return (T) data;
  }

  public Integer inactivity() {
    String value = headers.get(INACTIVITY_FIELD);
    return value != null ? Integer.valueOf(value) : null;
  }

  public boolean hasSignal(Signal signal) {
    String value = headers.get(SIGNAL_FIELD);
    return value != null && Integer.parseInt(value) == signal.code();
  }

  public Map<String, String> headers() {
    return headers;
  }

  @Override
  public String toString() {
    return new StringBuilder("GatewayMessage{")
        .append("headers=")
        .append(headers)
        .append(", data=")
        .append(data)
        .append('}')
        .toString();
  }

  public static class Builder {

    private Map<String, String> headers = new HashMap<>();
    private Object data;

    Builder() {}

    public Builder qualifier(String qualifier) {
      return header(QUALIFIER_FIELD, qualifier);
    }

    public Builder streamId(Long streamId) {
      return header(STREAM_ID_FIELD, streamId);
    }

    public Builder signal(Integer signal) {
      return header(SIGNAL_FIELD, signal);
    }

    public Builder signal(Signal signal) {
      return signal(signal.code());
    }

    public Builder inactivity(Integer inactivity) {
      return header(INACTIVITY_FIELD, inactivity);
    }

    public Builder data(Object data) {
      this.data = Objects.requireNonNull(data);
      return this;
    }

    /**
     * Add a header.
     *
     * @param key header name
     * @param value header value
     * @return self
     */
    public Builder header(String key, String value) {
      if (value != null) {
        headers.put(key, value);
      }
      return this;
    }

    /**
     * Add a header.
     *
     * @param key header name
     * @param value header value
     * @return self
     */
    public Builder header(String key, Object value) {
      if (value != null) {
        headers.put(key, value.toString());
      }
      return this;
    }

    /**
     * Add all headers.
     *
     * @param headers given headers
     * @return self
     */
    public Builder headers(Map<String, String> headers) {
      this.headers.putAll(headers);
      return this;
    }

    /**
     * Finally build the {@link GatewayMessage} from current builder.
     *
     * @return {@link GatewayMessage} with parameters from current builder.
     */
    public GatewayMessage build() {
      return new GatewayMessage(this);
    }
  }
}
