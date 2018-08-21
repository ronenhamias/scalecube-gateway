# Scalecube-gateway

<table text-align="top">
 <tr>
   <td>
    Scalecube-gateway allows act as the gate to the microservices cluster. its transparently discover and route requests to service endpoints.<br><br>
  <br><br>
  </td>
  <td>
  <img src="https://user-images.githubusercontent.com/1706296/44406217-ba417f00-a563-11e8-9d1c-8cea9261b274.png">
  </td>
</tr>
</table>

## API-Gateway: 

Basic API-Gateway example:

```java

    Microservices.builder()
        .seeds(....) // OPTIONAL: seed address list (if any to connect to)
        .services(...) // OPTIONAL: services (if any) as part of this node.
        
        // configure list of gateways plugins exposing the apis 
        .gateway(GatewayConfig.builder("http", HttpGateway.class).port(7070).build())
        .gateway(GatewayConfig.builder("ws", WebsocketGateway.class).port(8080).build())
        .gateway(GatewayConfig.builder("rsws", RSocketWebsocketGateway.class).port(9090).build())  
        
        .startAwait();
        
        // HINT: you can try connect using the api sandbox to these ports to try the api.
        // http://scalecube.io/api-sandbox/app/index.html
```


### Maven
    
**Service API-Gateway providers:**

releases: https://github.com/scalecube/scalecube-gateway/releases

* HTTP-Gateway - scalecube-gateway-http
* RSocket-Gateway - scalecube-gateway-rsocket-websocket
* WebSocket - scalecube-gateway-websocket


Binaries and dependency information for Maven can be found at http://search.maven.org.

https://mvnrepository.com/artifact/io.scalecube

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.scalecube/scalecube-services-api/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.scalecube/scalecube-services-api)

To add a dependency on ScaleCube Services using Maven, use the following:

```xml

 <!-- -------------------------------------------
    scalecube api-gateway providers:   
    please see: https://github.com/scalecube/scalecube-gateway
   ------------------------------------------- -->
   
  <!-- HTTP https://mvnrepository.com/artifact/io.scalecube/scalecube-gateway-http-->
  <dependency>
      <groupId>io.scalecube</groupId>
      <artifactId>scalecube-gateway-http</artifactId>
      <version>2.x.x</version>
    </dependency>

    <!-- RSocket WebSocket https://mvnrepository.com/artifact/io.scalecube/scalecube-gateway-rsocket-websocket -->
    <dependency>
      <groupId>io.scalecube</groupId>
      <artifactId>scalecube-gateway-rsocket-websocket</artifactId>
      <version>2.x.x</version>
    </dependency>

    <!-- WebSocket https://mvnrepository.com/artifact/io.scalecube/scalecube-gateway-websocket -->
    <dependency>
      <groupId>io.scalecube</groupId>
      <artifactId>scalecube-gateway-websocket</artifactId>
      <version>2.x.x</version>
    </dependency>

```
