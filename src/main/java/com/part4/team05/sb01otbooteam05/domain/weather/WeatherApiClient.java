package com.part4.team05.sb01otbooteam05.domain.weather;

import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherDto;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

//기상청 단기예보조회 OpenApi활용
@Component
public class WeatherApiClient {


  @Value("${weather.api.url}")
  private String baseUrl;

  @Value("${weather.api.serviceKey}")
  private String serviceKey;

  public WeatherDto getWeather(int x, int y) {
    String baseDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String baseTime = "0500";

    UriComponents uri = UriComponentsBuilder
        .fromUriString(baseUrl)
        .queryParam("ServiceKey", serviceKey)
        .queryParam("pageNo", 1)
        .queryParam("numOfRows", 1000)
        .queryParam("dataType", "JSON")
        .queryParam("base_date", baseDate)
        .queryParam("base_time", baseTime)
        .queryParam("nx", x)
        .queryParam("ny", y)
        .build(true);

    ResponseEntity<WeatherResponse> response = new RestTemplate().getForEntity(uri.toUri(), WeatherResponse.class);

    return convertToWeatherDto(response.getBody());
  }

  private WeatherDto convertToWeatherDto(WeatherResponse response) {
    // 파싱 로직 작성 (생략 가능)
    return new WeatherDto();
  }

}
