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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
@Slf4j
@Component
public class WeatherApiClient {

  @Value("${weather.api.url}")
  private String baseUrl;

  @Value("${weather.api.serviceKey}")
  private String serviceKey;

  // 재시도 최대 횟수
  private static final int MAX_RETRY_ON_NO_DATA = 3;

  // 재시도 지연 시간
  private static final long RETRY_DELAY_MS = 2000;

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

    // 기상청 api에 dataType=JSON 강제
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    // 기상청 api 응답 예외 처리 및 재시도
    for (int attempt = 1; attempt <= MAX_RETRY_ON_NO_DATA; attempt++) {
      try {
        ResponseEntity<WeatherResponse> response = new RestTemplate().exchange(uri.toUri(), HttpMethod.GET, entity, WeatherResponse.class);
        WeatherResponse weatherResponse = response.getBody();

        if (weatherResponse == null || weatherResponse.getResponse() == null || weatherResponse.getResponse().getHeader() == null) {
          throw new IllegalArgumentException("기상청 응답 구조가 유효하지 않습니다.");
        }

        String resultCode = weatherResponse.getResponse().getHeader().getResultCode();
        String resultMsg = weatherResponse.getResponse().getHeader().getResultMsg();

        if ("00".equals(resultCode)) {
          if (weatherResponse.getResponse().getBody() == null ||
              weatherResponse.getResponse().getBody().getItems() == null ||
              weatherResponse.getResponse().getBody().getItems().getItem() == null) {
            throw new IllegalArgumentException("기상청 응답이 유효하지 않습니다.");
          }

          log.info("기상청 응답 수신 완료: x={}, y={}, 항목 수={}",
              x, y, weatherResponse.getResponse().getBody().getItems().getItem().size());

          return parseForecastItems(weatherResponse);
        }

        if ("03".equals(resultCode)) {
          log.warn("NO_DATA 응답 (x={}, y={}, 시도 {}/{}): {}", x, y, attempt, MAX_RETRY_ON_NO_DATA, resultMsg);
          Thread.sleep(RETRY_DELAY_MS);
        } else {
          throw new IllegalArgumentException("기상청 응답 오류: " + resultMsg);
        }

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("재시도 대기 중 인터럽트 발생", e);
      }
    }

    throw new IllegalArgumentException("기상청 응답이 반복적으로 NO_DATA를 반환합니다.");
  }

  //기상청 API 응답 시간별로 파싱
  private ParsedForecastDto parseForecastItems(WeatherResponse response) {
    List<WeatherResponse.Item> items = response.getResponse().getBody().getItems().getItem();

    if (items == null || items.isEmpty()) {
      log.error("기상청 예보 항목이 비어 있음");
      throw new IllegalArgumentException("예보 항목이 없습니다.");
    }

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

