package com.part4.team05.sb01otbooteam05.domain.weather.batch;

import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import com.part4.team05.sb01otbooteam05.domain.weather.service.WeatherService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class WeatherItemProcessor implements ItemProcessor<Pair<Integer, Integer>, List<Weather>> {

  private final WeatherService weatherService;

  @Override
  public List<Weather> process(Pair<Integer, Integer> location) {
    int x = location.getLeft();
    int y = location.getRight();

    if(weatherService.existWeatherLocation(x, y)) {
      return null;
    }

    return weatherService.generateWeather(x, y);
  }

}
