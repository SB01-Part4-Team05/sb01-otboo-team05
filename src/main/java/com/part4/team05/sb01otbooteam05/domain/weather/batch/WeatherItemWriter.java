package com.part4.team05.sb01otbooteam05.domain.weather.batch;

import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import com.part4.team05.sb01otbooteam05.domain.weather.repository.WeatherRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class WeatherItemWriter implements ItemWriter<List<Weather>> {

  private final WeatherRepository weatherRepository;

  @Override
  public void write(Chunk<? extends List<Weather>> items) {
    for (List<Weather> weatherList : items) {
      if (weatherList != null && !weatherList.isEmpty()) {
        weatherRepository.saveAll(weatherList);
      }
    }
  }

}
