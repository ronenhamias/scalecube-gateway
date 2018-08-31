package io.scalecube.gateway.examples;

import io.scalecube.services.api.ServiceMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class GreetingServiceCancelCallback implements GreetingService {

  private final GreetingService greetingService = new GreetingServiceImpl();
  private final Runnable onCancel;

  public GreetingServiceCancelCallback(Runnable onCancel) {
    this.onCancel = onCancel;
  }

  @Override
  public Mono<String> one(String name) {
    return greetingService.one(name).doOnCancel(onCancel);
  }

  @Override
  public Mono<ServiceMessage> oneMessage(ServiceMessage request) {
    return greetingService.oneMessage(request).doOnCancel(onCancel);
  }

  @Override
  public Flux<Long> manyStream(Long cnt) {
    return greetingService.manyStream(cnt).doOnCancel(onCancel);
  }

  @Override
  public Mono<String> failingOne(String name) {
    return greetingService.failingOne(name).doOnCancel(onCancel);
  }

  @Override
  public Flux<String> many(String name) {
    return greetingService.many(name).doOnCancel(onCancel);
  }

  @Override
  public Flux<String> failingMany(String name) {
    return greetingService.failingMany(name).doOnCancel(onCancel);
  }

  @Override
  public Mono<GreetingResponse> pojoOne(GreetingRequest request) {
    return greetingService.pojoOne(request).doOnCancel(onCancel);
  }

  @Override
  public Flux<GreetingResponse> pojoMany(GreetingRequest request) {
    return greetingService.pojoMany(request).doOnCancel(onCancel);
  }

  @Override
  public Mono<String> emptyOne(String name) {
    return greetingService.emptyOne(name).doOnCancel(onCancel);
  }

  @Override
  public Flux<String> emptyMany(String name) {
    return greetingService.emptyMany(name).doOnCancel(onCancel);
  }

  @Override
  public Mono<String> neverOne(String name) {
    return greetingService.neverOne(name).doOnCancel(onCancel);
  }

  @Override
  public Mono<String> delayOne(String name) {
    return greetingService.delayOne(name).doOnCancel(onCancel);
  }

  @Override
  public Flux<String> delayMany(String name) {
    return greetingService.delayMany(name).doOnCancel(onCancel);
  }

  @Override
  public Flux<Long> requestInfiniteStream(StreamRequest request) {
    return greetingService.requestInfiniteStream(request).doOnCancel(onCancel);
  }

  @Override
  public Flux<ServiceMessage> requestInfiniteMessageStream(ServiceMessage request) {
    return greetingService.requestInfiniteMessageStream(request).doOnCancel(onCancel);
  }

  @Override
  public Flux<ServiceMessage> rawStream(ServiceMessage request) {
    return greetingService.rawStream(request).doOnCancel(onCancel);
  }

  @Override
  public Flux<Long> broadcastStream() {
    return greetingService.broadcastStream().doOnCancel(onCancel);
  }

  @Override
  public Flux<ServiceMessage> rawBroadcastStream() {
    return greetingService.rawBroadcastStream().doOnCancel(onCancel);
  }
}
