package com.part4.team05.sb01otbooteam05.domain.weather.batch.listener;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomChunkListener implements ChunkListener {

  private final MeterRegistry meterRegistry;

  @Override
  public void afterChunkError(ChunkContext context) {
    String stepName = context.getStepContext().getStepName();
    meterRegistry.counter(
        "batch.chunk.error.count",
        "batch_step", stepName
    ).increment();
  }

  @Override
  public void afterChunk(ChunkContext context) {
    String stepName = context.getStepContext().getStepName();
    meterRegistry.counter(
        "batch.chunk.count",
        "batch_step", stepName
    ).increment();
  }
}
