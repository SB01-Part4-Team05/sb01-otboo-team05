package com.part4.team05.sb01otbooteam05.domain.weather.batch;

import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import com.part4.team05.sb01otbooteam05.domain.weather.service.WeatherService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class WeatherItemProcessor implements ItemProcessor<Pair<Integer, Integer>, List<Weather>> {

  private final WeatherService weatherService;

  @Override
  public List<Weather> process(Pair<Integer, Integer> location) {
    int x = location.getLeft();
    int y = location.getRight();

    LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);

    if(weatherService.existWeather(x, y, now)) {
      log.info("이미 날씨 데이터 존재: x={}, y={}, forecastedAt = {}", x, y, now);
      return null;
    }

    log.info("날씨 데이터 생성 시작: x={}, y={}", x, y);
    List<Weather> weatherList = weatherService.generateWeather(x, y);
    log.info("날씨 데이터 생성 완료: x={}, y={}, 건수={}", x, y, weatherList.size());
    return weatherList;
  }

}
