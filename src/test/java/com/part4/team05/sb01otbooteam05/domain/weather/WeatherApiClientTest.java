package com.part4.team05.sb01otbooteam05.domain.weather;

import com.part4.team05.sb01otbooteam05.domain.weather.dto.ParsedForecastDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class WeatherApiClientTest {

  @Autowired
  private WeatherApiClient weatherApiClient;

  @Test
  void fetchForecast() {
    //given
    int x = 60;
    int y = 127;

    //when
    ParsedForecastDto dto = weatherApiClient.fetchForecast(x, y);

    //then
    System.out.println("Forecasted at :" + dto.forecastedDateTime());
    dto.forecastMap().forEach((time, map) -> {
      System.out.println("예보 시간: " + time + " / 값: " + map);
    });

    assert !dto.forecastMap().isEmpty();
  }

}
