package com.part4.team05.sb01otbooteam05.domain.weather.batch.listener;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomItemReadListener implements ItemReadListener<Object> {

  private final MeterRegistry meterRegistry;

  @Override
  public void afterRead(@NonNull Object item) {
    meterRegistry.counter(
        "batch.item.read.count"
    ).increment();
  }
}
