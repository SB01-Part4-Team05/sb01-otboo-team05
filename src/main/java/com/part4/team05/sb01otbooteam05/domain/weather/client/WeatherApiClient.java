package com.part4.team05.sb01otbooteam05.domain.weather.client;

import com.part4.team05.sb01otbooteam05.domain.weather.dto.ParsedForecastDto;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherResponse;
import com.part4.team05.sb01otbooteam05.domain.weather.utils.BaseTimeUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownContentTypeException;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

//기상청 단기예보조회 OpenApi활용
@Component
public class WeatherApiClient {

  @Value("${weather.api.url}")
  private String baseUrl;

  @Value("${weather.api.serviceKey}")
  private String serviceKey;

  //기상청 API 호출 -> 시간별 예보 정보를 WeatherResponse dto로 받음
  @Retryable(
      retryFor = { RestClientException.class, UnknownContentTypeException.class },
      noRetryFor = { IllegalArgumentException.class },
      maxAttempts = 3,
      backoff = @Backoff(delay = 2000)
  )
  public ParsedForecastDto fetchForecast(int x, int y) {
    Pair<LocalDate, String> baseInfo = BaseTimeUtils.getLatestBaseDateTime();

    UriComponents uri = UriComponentsBuilder
        .fromUriString(baseUrl)
        .queryParam("ServiceKey", serviceKey)
        .queryParam("pageNo", 1)
        .queryParam("numOfRows", 1000)
        .queryParam("dataType", "JSON")
        .queryParam("base_date", baseInfo.getLeft().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
        .queryParam("base_time", baseInfo.getRight())
        .queryParam("nx", x)
        .queryParam("ny", y)
        .build(true);

    ResponseEntity<WeatherResponse> response = new RestTemplate().getForEntity(uri.toUri(),
        WeatherResponse.class);

    // 응답 없을 시 예외 발생
    if (response.getBody() == null) {
      throw new IllegalArgumentException("기상청 응답이 비어있습니다.");
    }

    return parseForecastItems(response.getBody());
  }

  //기상청 API 응답 시간별로 파싱
  private ParsedForecastDto parseForecastItems(WeatherResponse response) {
    List<WeatherResponse.Item> items = response.getResponse().getBody().getItems().getItem();

    // 예보 등록 기준 시간
    WeatherResponse.Item firstItem = items.get(0);
    LocalDateTime forecastedDateTime = BaseTimeUtils.toDateTime(firstItem.getBaseDate(),
        firstItem.getBaseTime());

    // 시간별 생성
    Map<LocalDateTime, Map<String, String>> forecastMap = new HashMap<>();

    for (WeatherResponse.Item item : items) {
      //예보 시간
      LocalDateTime forecastDateTime = BaseTimeUtils.toDateTime(item.getFcstDate(),
          item.getFcstTime());

      forecastMap
          .computeIfAbsent(forecastDateTime, k -> new HashMap<>())
          .put(item.getCategory(), item.getFcstValue());

    }
    return new ParsedForecastDto(forecastedDateTime, forecastMap);
  }

}

