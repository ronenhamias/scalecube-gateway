package io.scalecube.gateway.benchmarks;

import io.scalecube.benchmarks.BenchmarksSettings;
import io.scalecube.gateway.core.GatewayMessage;
import io.scalecube.gateway.core.GatewayMessageCodec;

import com.codahale.metrics.Timer;

import io.netty.buffer.ByteBuf;

import java.util.concurrent.TimeUnit;

public class DecodeGatewayMessageBenchmarkRunner {

  public static void main(String[] args) {
    BenchmarksSettings settings = BenchmarksSettings.from(args).durationUnit(TimeUnit.NANOSECONDS).build();
    new GatewayMessageCodecBenchmarkState(settings).runForSync(state -> {

      GatewayMessageCodec codec = state.codec();
      ByteBuf bb = state.byteBufExample();
      Timer timer = state.timer("timer");

      return i -> {
        Timer.Context timerContext = timer.time();
        GatewayMessage gatewayMessage = codec.decode(bb);
        timerContext.stop();
        return gatewayMessage;
      };
    });
  }
}
