package io.scalecube.gateway.examples;

import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.api.ServiceMessage.Builder;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.stream.LongStream;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class GreetingServiceImpl implements GreetingService {

  private static final String SERVICE_RECEIVED_TIME_HEADER = "srv-recd-time";

  private Flux<Long> source = Flux.fromStream(LongStream.range(0, Long.MAX_VALUE).boxed()).share();

  @Override
  public Mono<String> one(String name) {
    return Mono.just("Echo:" + name);
  }

  @Override
  public Mono<ServiceMessage> oneMessage(ServiceMessage request) {
    return Mono.defer(
        () ->
            Mono.just(
                ServiceMessage.from(request)
                    .header(
                        SERVICE_RECEIVED_TIME_HEADER, String.valueOf(System.currentTimeMillis()))
                    .build()));
  }

  @Override
  public Flux<Long> manyStream(Long cnt) {
    return Flux.fromStream(LongStream.range(0, cnt).boxed()).publishOn(Schedulers.parallel());
  }

  @Override
  public Mono<String> failingOne(String name) {
    return Mono.error(new RuntimeException(name));
  }

  @Override
  public Flux<String> many(String name) {
    return Flux.interval(Duration.ofMillis(100)).map(i -> "Greeting (" + i + ") to: " + name);
  }

  @Override
  public Flux<String> failingMany(String name) {
    return Flux.push(
        sink -> {
          sink.next("Echo:" + name);
          sink.next("Echo:" + name);
          sink.error(new RuntimeException("Echo:" + name));
        });
  }

  @Override
  public Mono<GreetingResponse> pojoOne(GreetingRequest request) {
    return one(request.getText()).map(GreetingResponse::new);
  }

  @Override
  public Flux<GreetingResponse> pojoMany(GreetingRequest request) {
    return many(request.getText()).map(GreetingResponse::new);
  }

  @Override
  public Mono<String> emptyOne(String name) {
    return Mono.empty();
  }

  @Override
  public Flux<String> emptyMany(String name) {
    return Flux.empty();
  }

  @Override
  public Mono<String> neverOne(String name) {
    return Mono.never();
  }

  @Override
  public Mono<String> delayOne(String name) {
    return Mono.delay(Duration.ofSeconds(1)).then(Mono.just(name));
  }

  @Override
  public Flux<String> delayMany(String name) {
    return Flux.interval(Duration.ofMillis(500), Duration.ofSeconds(2)).map(i -> name);
  }

  @Override
  public Flux<Long> requestInfiniteStream(StreamRequest request) {
    Flux<Flux<Long>> fluxes =
        Flux.interval(Duration.ofMillis(request.getIntervalMillis()))
            .map(
                tick ->
                    Flux.create(
                        s -> {
                          for (int i = 0; i < request.getMessagesPerInterval(); i++) {
                            s.next(System.currentTimeMillis());
                          }
                          s.complete();
                        }));

    return Flux.concat(fluxes).publishOn(Schedulers.parallel()).onBackpressureDrop();
  }

  @Override
  public Flux<ServiceMessage> requestInfiniteMessageStream(ServiceMessage request) {
    return Flux.defer(
        () -> {
          Duration interval =
              Duration.ofMillis(Long.parseLong(request.header("executionTaskInterval")));
          int messagesPerInterval =
              Integer.parseInt(request.header("messagesPerExecutionInterval"));

          Flux<Flux<ServiceMessage>> fluxes =
              Flux.interval(interval).map(tick -> emitServiceMessages(messagesPerInterval));

          return Flux.concat(fluxes)
              .publishOn(Schedulers.parallel(), Integer.MAX_VALUE)
              .onBackpressureDrop();
        });
  }

  @Override
  public Flux<ServiceMessage> rawStream(ServiceMessage request) {
    Callable<ServiceMessage> callable =
        () -> {
          Builder builder = ServiceMessage.builder();
          return builder.header(TIMESTAMP_KEY, "" + System.currentTimeMillis()).build();
        };
    return Mono.fromCallable(callable).subscribeOn(Schedulers.parallel()).repeat();
  }

  @Override
  public Flux<Long> broadcastStream() {
    return source.subscribeOn(Schedulers.parallel()).map(i -> System.currentTimeMillis());
  }

  @Override
  public Flux<ServiceMessage> rawBroadcastStream() {
    return source
        .subscribeOn(Schedulers.parallel())
        .map(
            i ->
                ServiceMessage.builder()
                    .header(TIMESTAMP_KEY, Long.toString(System.currentTimeMillis()))
                    .build());
  }

  private Flux<ServiceMessage> emitServiceMessages(int messagesPerInterval) {
    return Flux.create(
        fluxSink -> {
          for (int i = 0; i < messagesPerInterval; i++) {
            fluxSink.next(
                ServiceMessage.builder()
                    .header(
                        SERVICE_RECEIVED_TIME_HEADER, String.valueOf(System.currentTimeMillis()))
                    .build());
          }
          fluxSink.complete();
        });
  }
}
