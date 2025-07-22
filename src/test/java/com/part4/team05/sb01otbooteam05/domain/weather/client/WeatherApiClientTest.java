package com.part4.team05.sb01otbooteam05.domain.weather.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.part4.team05.sb01otbooteam05.domain.weather.dto.ParsedForecastDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = WeatherApiClient.class)
@ActiveProfiles("test")
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

    assertNotNull(dto);
    assertNotNull(dto.forecastedDateTime());
    assertFalse(dto.forecastMap().isEmpty(), "예보 정보가 비어있지 않아야 합니다.");

    //then
    dto.forecastMap().forEach((time, map) -> {
      System.out.println("예보 시간: " + time + " / 값: " + map);
    });
  }
}
