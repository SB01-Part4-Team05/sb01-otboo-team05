package com.part4.team05.sb01otbooteam05.domain.weather.batch.writer;

import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import com.part4.team05.sb01otbooteam05.domain.weather.repository.WeatherRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class WeatherItemWriter implements ItemWriter<List<Weather>> {

  private final WeatherRepository weatherRepository;

  @Override
  public void write(Chunk<? extends List<Weather>> items) {

    int total = 0;

    for (List<Weather> weatherList : items) {
      if (weatherList != null && !weatherList.isEmpty()) {
        weatherRepository.saveAll(weatherList);
        total += weatherList.size();
      }
    }
    log.info("날씨 데이터 저장 완료: 한 청크 내 총 {}건", total);
  }

}
