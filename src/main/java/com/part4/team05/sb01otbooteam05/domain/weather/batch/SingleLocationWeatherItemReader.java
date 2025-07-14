package com.part4.team05.sb01otbooteam05.domain.weather.batch;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class SingleLocationWeatherItemReader implements ItemReader<Pair<Integer, Integer>> {

  @Value("#{jobParameters['x']}")
  private Integer x;

  @Value("#{jobParameters['y']}")
  private Integer y;

  private boolean read = false;

  @Override
  public Pair<Integer, Integer> read() {
    if(!read) {
      read = true;
      return Pair.of(x, y);
    }
    return null;
  }
}
