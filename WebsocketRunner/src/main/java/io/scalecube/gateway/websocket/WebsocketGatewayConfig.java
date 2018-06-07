package io.scalecube.gateway.websocket;

public class WebsocketGatewayConfig {

  private int websocketPort = 8080;
  private String seedAddress = "localhost:4801";

  public WebsocketGatewayConfig() {}

  public int getWebsocketPort() {
    return websocketPort;
  }

  public void setWebsocketPort(int websocketPort) {
    this.websocketPort = websocketPort;
  }

  public String getSeedAddress() {
    return seedAddress;
  }

  public void setSeedAddress(String seedAddress) {
    this.seedAddress = seedAddress;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("WebsocketGatewayConfig{");
    sb.append("websocketPort=").append(websocketPort);
    sb.append(", seedAddress='").append(seedAddress).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
