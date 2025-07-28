package com.part4.team05.sb01otbooteam05.domain.weather.batch.writer;

import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import com.part4.team05.sb01otbooteam05.domain.weather.mapper.WeatherJdbcMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class WeatherItemWriter implements ItemWriter<List<Weather>> {

  private static final String INSERT_SQL = """
      INSERT INTO weathers (
        id, location_x, location_y, forecasted_at, forecast_at,
        sky_status, precipitation_type, precipitation_amount,
        precipitation_probability, humidity_current, humidity_compared_to_day_before,
        temperature_current, temperature_compared_to_day_before,
        temperature_min, temperature_max,
        wind_speed, wind_speed_as_word
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      ON CONFLICT (location_x, location_y, forecasted_at, forecast_at) DO NOTHING
      """;

  private final JdbcTemplate jdbcTemplate;

  @Override
  public void write(Chunk<? extends List<Weather>> items) {
    log.info("Writer 실행 스레드: {}", Thread.currentThread().getName());

    log.info("Writer received {} items", items.size());
    List<Object[]> objects = new ArrayList<>();
    for (List<Weather> weatherList : items) {
      if (weatherList != null && !weatherList.isEmpty()) {
        for(Weather weather : weatherList) {
          objects.add(WeatherJdbcMapper.toJdbcRow(weather));
        }
      }
    }
    int[] result = jdbcTemplate.batchUpdate(INSERT_SQL, objects);

    int totalInserted = Arrays.stream(result).sum();
    log.info("날씨 데이터 저장 완료: 총 {}건", totalInserted);
  }

}
