package com.part4.team05.sb01otbooteam05.domain.weather.batch.processor;

import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import com.part4.team05.sb01otbooteam05.domain.weather.service.WeatherService;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
@Setter
public class WeatherItemProcessor implements ItemProcessor<Pair<Integer, Integer>, List<Weather>> {

  private final WeatherService weatherService;
  private Set<Pair<Integer, Integer>> existLocationSet = Collections.emptySet();

  @Override
  public List<Weather> process(@NonNull Pair<Integer, Integer> location) {
    try {
      log.info("🟢 process 진입");

      int x = location.getLeft();
      int y = location.getRight();

      Pair<Integer, Integer> key = Pair.of(x, y);
      log.info("🟡 좌표 확인: x={}, y={}", x, y);
      log.info("🟠 존재 여부: {}", existLocationSet.contains(key));

      if (existLocationSet.contains(key)) {
        return Collections.emptyList();
      }

      log.info("🔵 날씨 생성 시도: x={}, y={}", x, y);
      return weatherService.generateWeather(x, y);
    } catch (Exception e) {
      log.error("❌ process() 내부에서 예외 발생", e);
      return Collections.emptyList();
    }
  }
}
