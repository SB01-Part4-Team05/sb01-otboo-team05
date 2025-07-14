package com.part4.team05.sb01otbooteam05.domain.weather.batch;

import com.part4.team05.sb01otbooteam05.domain.weather.exception.InvalidDataException;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class SingleLocationWeatherItemReader implements ItemReader<Pair<Integer, Integer>> {

  @Value("#{jobParameters['x']}")
  private String xStr;

  @Value("#{jobParameters['y']}")
  private String yStr;

  private boolean read = false;

  @Override
  public Pair<Integer, Integer> read() {
    if(!read) {
      read = true;
      try {
        int x = Integer.parseInt(xStr);
        int y = Integer.parseInt(yStr);
        return Pair.of(x, y);
      } catch (Exception e) {
        throw new InvalidDataException("잘못된 값에 따른 파싱 실패");
      }
    }
    return null;
  }
}
