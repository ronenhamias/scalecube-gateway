package io.scalecube.gateway.websocket;

import io.scalecube.gateway.clientsdk.Client;
import io.scalecube.gateway.clientsdk.ClientSettings;
import io.scalecube.gateway.clientsdk.codec.WebsocketGatewayMessageCodec;
import io.scalecube.gateway.clientsdk.websocket.WebsocketClientTransport;
import io.scalecube.gateway.examples.GreetingService;
import io.scalecube.services.codec.DataCodec;
import java.time.Duration;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.resources.LoopResources;

public class WebsocketClientTransportTest {

  public static void main(String[] args) throws InterruptedException {
    String contentType = "application/json";
    ClientSettings settings =
        ClientSettings.builder().contentType(contentType).host("localhost").port(7070).build();
    LoopResources loopResources = LoopResources.create("worker");

    WebsocketGatewayMessageCodec codec =
        new WebsocketGatewayMessageCodec(DataCodec.getInstance(contentType));

    WebsocketClientTransport transport =
        new WebsocketClientTransport(settings, codec, loopResources);

    Client client = new Client(transport, codec);
    Disposable disposable = client.forService(GreetingService.class).neverOne("hello").subscribe();

    Mono.delay(Duration.ofSeconds(2)).doOnTerminate(disposable::dispose).subscribe();

    Thread.currentThread().join();
  }
}
