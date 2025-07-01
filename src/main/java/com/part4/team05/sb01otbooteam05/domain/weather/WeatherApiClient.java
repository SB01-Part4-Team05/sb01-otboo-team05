package com.part4.team05.sb01otbooteam05.domain.weather;

import static java.lang.Double.parseDouble;

import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.HumidityDto;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.PrecipitationDto;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.TemperatureDto;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherAPILocation;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherDto;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherResponse;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WindSpeedDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  //기상청 API 요청
  public List<WeatherDto> getWeather(int x, int y) {
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

    return convertToWeatherDto(response.getBody());
  }

  //기상청 API 응답 파싱
  private List<WeatherDto> convertToWeatherDto(WeatherResponse response) {
    List<WeatherResponse.Item> items = response.getResponse().getBody().getItems().getItem();

    // 시간별 생성
    Map<LocalDateTime, Map<String, String>> forcecastMap = new HashMap<>();

    // LocalDateTime 으로 시간 변환 포맷
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    for (WeatherResponse.Item item : items) {
      String key = item.getFcstDate() + item.getFcstTime();
      LocalDateTime forecastDateTime = LocalDateTime.parse(key, dateTimeFormatter);
      forcecastMap.computeIfAbsent(forecastDateTime, k -> new HashMap<>())
          .put(item.getCategory(), item.getFcstValue());
    }

    // 예보 등록 시간
    String baseTime = items.get(0).getBaseDate() + items.get(0).getBaseTime();
    LocalDateTime forecastedDateTime = LocalDateTime.parse(baseTime, dateTimeFormatter);

    // 날짜별 모든 시간 TMP(온도) 모아서 min/max 계산
    Map<LocalDate, List<Double>> tmpPerDay = new HashMap<>();
    for (Map.Entry<LocalDateTime, Map<String, String>> entry : forcecastMap.entrySet()) {
      Map<String, String> catMap = entry.getValue();
      if (catMap.containsKey("TMP")) {
        double tmp = parseDouble(catMap.get("TMP"));
        tmpPerDay.computeIfAbsent(entry.getKey().toLocalDate(), k -> new ArrayList<>()).add(tmp);
      }
    }

    Map<LocalDate, Double> minTempMap = new HashMap<>();
    Map<LocalDate, Double> maxTempMap = new HashMap<>();
    tmpPerDay.forEach((date, temps) -> {
      minTempMap.put(date, temps.stream().min(Double::compareTo).orElse(null));
      maxTempMap.put(date, temps.stream().max(Double::compareTo).orElse(null));
    });

    List<WeatherDto> result = new ArrayList<>();

    for (Map.Entry<LocalDateTime, Map<String, String>> entry : forcecastMap.entrySet()) {
      LocalDateTime dateTime = entry.getKey();
      Map<String, String> values = entry.getValue();

      // tmp -> 온도, reh -> 습도
      double tmp = parseDouble(values.get("TMP"));
      double reh = parseDouble(values.get("REH"));

      // 전날의 데이터
      Map<String, String> yesterdayData = forcecastMap.get(dateTime.minusDays(1));
      Double tmpDiff = (yesterdayData != null && yesterdayData.containsKey("TMP"))
          ? tmp - parseDouble(yesterdayData.get("TMP"))
          : null;
      Double rehDiff = (yesterdayData != null && yesterdayData.containsKey("REH"))
          ? tmp - parseDouble(yesterdayData.get("REH"))
          : null;

      //날씨 위치 구하기를... 메서드를 빼야 하나 이런건 서비스에서 해야 할 것 같은데


      // TemperatureDto 생성
      TemperatureDto temperatureDto = new TemperatureDto(
          tmp,
          tmpDiff,
          minTempMap.get(dateTime.toLocalDate()),
          maxTempMap.get(dateTime.toLocalDate())
      );

      //HumidityDto 생성
      HumidityDto humidityDto = new HumidityDto(
          reh,
          rehDiff
      );

      // WindSpeedDto 생성
      WindSpeedDto windSpeedDto = new WindSpeedDto(
          parseDouble(values.get("WSD")),
          WeatherCategoryMapper.toWindSpeedAsWord(values.get("WSD"))
      );

      // PrecipitationDto 생성
      PrecipitationDto precipitationDto = new PrecipitationDto(
          WeatherCategoryMapper.toPrecipitationType(values.get("PTY")),
          parseDouble(values.get("PCP")),
          parseDouble(values.get("POP"))
      );

      WeatherDto dto = new WeatherDto(
          UUID.randomUUID(), //여기가 아니라 날씨 엔티티를 먼저 넣어야 하는거 아닐까
          forecastedDateTime,
          dateTime,
          location,
          WeatherCategoryMapper.toSkyStatusType(values.get("SKY")),
          precipitationDto,
          humidityDto,
          temperatureDto,
          windSpeedDto
      );

      result.add(dto);
    }

    return result;
  }

}
