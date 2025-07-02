package com.part4.team05.sb01otbooteam05.domain.weather;

import com.part4.team05.sb01otbooteam05.domain.weather.dto.ParsedForecastDto;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
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

  // LocalDateTime 으로 시간 변환 포맷
  private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
      "yyyyMMddHHmm");

  //기상청 API 호출 -> 시간별 예보 정보를 Map으로 반환
  public ParsedForecastDto fetchForecast(int x, int y) {
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

    ResponseEntity<WeatherResponse> response = new RestTemplate().getForEntity(uri.toUri(),
        WeatherResponse.class);

    return parseForecastItems(response.getBody());
  }

  //기상청 API 응답 시간별로 파싱
  private ParsedForecastDto parseForecastItems(WeatherResponse response) {
    List<WeatherResponse.Item> items = response.getResponse().getBody().getItems().getItem();

    // 예보 등록 기준 시간
    LocalDateTime forecastedDateTime = LocalDateTime
        .parse(items.get(0).getBaseDate() + items.get(0).getBaseTime(), dateTimeFormatter);

    // 시간별 생성
    Map<LocalDateTime, Map<String, String>> forcecastMap = new HashMap<>();

    for (WeatherResponse.Item item : items) {
      String key = item.getFcstDate() + item.getFcstTime();
      //예보 시간
      LocalDateTime forecastDateTime = LocalDateTime.parse(key, dateTimeFormatter);

      forcecastMap
          .computeIfAbsent(forecastDateTime, k -> new HashMap<>())
          .put(item.getCategory(), item.getFcstValue());

    }
    return new ParsedForecastDto(forecastedDateTime, forcecastMap);
  }

}

