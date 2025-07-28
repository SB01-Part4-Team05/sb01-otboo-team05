package com.part4.team05.sb01otbooteam05.domain.weather.batch.listener;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomItemProcessListener implements ItemProcessListener<Object, Object> {

  private final MeterRegistry meterRegistry;

  @Override
  public void afterProcess(@NonNull Object item, Object result) {
    meterRegistry.counter(
        "batch.item.process.count"
    ).increment();
  }
}
