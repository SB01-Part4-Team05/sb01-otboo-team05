package com.part4.team05.sb01otbooteam05.domain.weather.service;

import static java.lang.Double.*;

import com.part4.team05.sb01otbooteam05.domain.user.service.KakaoApiService;
import com.part4.team05.sb01otbooteam05.domain.user.util.LccGridConverter;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherAPILocation;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherDto;
import com.part4.team05.sb01otbooteam05.domain.weather.mapper.WeatherMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.part4.team05.sb01otbooteam05.domain.weather.WeatherApiClient;
import com.part4.team05.sb01otbooteam05.domain.weather.mapper.WeatherCategoryMapper;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.ParsedForecastDto;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import com.part4.team05.sb01otbooteam05.domain.weather.repository.WeatherRepository;
import com.part4.team05.sb01otbooteam05.domain.weather.exception.WeatherNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

  private final WeatherApiClient weatherApiClient;
  private final WeatherRepository weatherRepository;
  private final KakaoApiService kakaoApiService;

  @Transactional
  public List<Weather> generateWeather(int x, int y) {
    //예보 위치 x,y 값 받기
    ParsedForecastDto parsedForecastDto = weatherApiClient.fetchForecast(x, y);
    return parsedForecastDtoToWeathers(parsedForecastDto, x, y);
  }

  // 기상청 API 응답값 저장
  public List<Weather> parsedForecastDtoToWeathers(ParsedForecastDto parsedForecastDto, int x,
      int y) {

    //예보 등록 기준 시간
    LocalDateTime forecastedAt = parsedForecastDto.forecastedDateTime();

    //예보 정보 담기
    Map<LocalDateTime, Map<String, String>> forecastMap = parsedForecastDto.forecastMap();

    // 날짜별 모든 시간 TMP(온도) 수집
    Map<LocalDate, Double> minTemps = getMinTemps(forecastMap);
    Map<LocalDate, Double> maxTemps = getMaxTemps(forecastMap);

    // 날씨 리스트 생성
    List<Weather> weathers = new ArrayList<>();

    // 시간별 날씨 엔티티 생성 작업
    for (Map.Entry<LocalDateTime, Map<String, String>> entry : forecastMap.entrySet()) {
      LocalDateTime forecastAt = entry.getKey();
      Map<String, String> values = entry.getValue();

      double tmp = parseDouble(values.get("TMP"));
      double reh = parseDouble(values.get("REH"));

      Double tmpDiff = null;
      Double rehDiff = null;

      LocalDateTime yesterdayForecastAt = forecastAt.minusDays(1);

      Map<String, String> yesterdayData = forecastMap.get(yesterdayForecastAt);

      if (yesterdayData != null) {
        // 기상청 응답 안에 전날 데이터가 있으면 그걸로 계산
        if (yesterdayData.containsKey("TMP")) {
          tmpDiff = tmp - parseDouble(yesterdayData.get("TMP"));
        }
        if (yesterdayData.containsKey("REH")) {
          rehDiff = reh - parseDouble(yesterdayData.get("REH"));
        }
      } else {
        // forecastMap에 전날 정보가 없으면 DB에서 조회
        Optional<Weather> yesterday = weatherRepository.findByLocationXAndLocationYAndForecastAt(x, y, yesterdayForecastAt);

        tmpDiff = yesterday.map(w -> tmp - w.getTemperatureCurrent()).orElse(null);
        rehDiff = yesterday.map(w -> reh - w.getHumidityCurrent()).orElse(null);
      }

      // Weather 엔티티 생성
      Weather weather = Weather.builder()
          .locationX(x)
          .locationY(y)
          .forecastedAt(forecastedAt)
          .forecastAt(forecastAt)
          .skyStatusType(WeatherCategoryMapper.toSkyStatusType(values.get("SKY")))
          .precipitationType(WeatherCategoryMapper.toPrecipitationType(values.get("PTY")))
          .precipitationAmount(WeatherCategoryMapper.toPrecipitation(values.get("PCP")))
          .precipitationProbability(parseDouble(values.get("POP")))
          .humidityCurrent(reh)
          .humidityComparedToDayBefore(rehDiff)
          .temperatureCurrent(tmp)
          .temperatureComparedToDayBefore(tmpDiff)
          .temperatureMin(minTemps.get(forecastAt.toLocalDate()))
          .temperatureMax(maxTemps.get(forecastAt.toLocalDate()))
          .windSpeed(parseDouble(values.get("WSD")))
          .windSpeedAsWord(WeatherCategoryMapper.toWindSpeedAsWord(values.get("WSD")))
          .build();

      weathers.add(weather);
    }
    return weathers;
  }

  // 날짜 별 최저 온도
  private Map<LocalDate, Double> getMinTemps(Map<LocalDateTime, Map<String, String>> forecastMap) {
    Map<LocalDate, List<Double>> tmpPerDay = groupTmpByDate(forecastMap);
    Map<LocalDate, Double> result = new HashMap<>();
    tmpPerDay.forEach((date, list) ->
        result.put(date, list.stream().min(Double::compareTo).orElse(null))
    );
    return result;
  }

  // 날짜 별 최고 온도
  private Map<LocalDate, Double> getMaxTemps(Map<LocalDateTime, Map<String, String>> forecastMap) {
    Map<LocalDate, List<Double>> tmpPerDay = groupTmpByDate(forecastMap);
    Map<LocalDate, Double> result = new HashMap<>();
    tmpPerDay.forEach((date, list) ->
        result.put(date, list.stream().max(Double::compareTo).orElse(null))
    );
    return result;
  }

  // tmp 데이터 그룹화
  private Map<LocalDate, List<Double>> groupTmpByDate(
      Map<LocalDateTime, Map<String, String>> forecastMap) {
    Map<LocalDate, List<Double>> tmpPerDay = new HashMap<>();

    for (Map.Entry<LocalDateTime, Map<String, String>> entry : forecastMap.entrySet()) {
      LocalDate date = entry.getKey().toLocalDate();
      String value = entry.getValue().get("TMP");

      if (value != null && !value.isBlank()) {
        try {
          double tmp = parseDouble(value);
          tmpPerDay.computeIfAbsent(date, d -> new ArrayList<>()).add(tmp);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("파싱 실패: " + value); //todo 예외처리
        }
      }
    }

    return tmpPerDay;
  }

  public boolean existWeatherLocation(int x, int y) {
    return weatherRepository.existsByLocationXAndLocationY(x, y);
  }

  @Transactional(readOnly = true)
  public Weather getWeatherEntityByIdOrThrow(UUID weatherId) {
    return weatherRepository.findById(weatherId)
        .orElseThrow(() -> WeatherNotFoundException.withId(weatherId));
  }

  public List<WeatherDto> getWeathers(double longitude, double latitude) {

    WeatherAPILocation weatherAPILocation = getWeatherAPILocation(longitude, latitude);
    LocalDateTime now = LocalDateTime.now();
    LocalTime requestedTime = now.toLocalTime().truncatedTo(ChronoUnit.HOURS);

    List<WeatherDto> result = new ArrayList<>();

    // 기상청 날씨 정보가 3일 뒤부터는 매 시간마다 정보를 주지 않아 00시로 고정
    for (int i = 0; i <= 4; i++) {
      LocalDate targetDate = now.toLocalDate().plusDays(i);
      LocalTime targetTime = i < 2 ? requestedTime : LocalTime.MIDNIGHT;
      LocalDateTime targetForecastAt = LocalDateTime.of(targetDate, targetTime);

      log.info("요청 기준 forecastAt (targetForecastAt): {}, x = {}, y = {}", targetForecastAt, weatherAPILocation.x(), weatherAPILocation.y());

      weatherRepository.findByLocationXAndLocationYAndForecastAt(
          weatherAPILocation.x(), weatherAPILocation.y(), targetForecastAt)
          .stream()
          .filter(weather -> weather.getForecastAt().equals(targetForecastAt))
          .findFirst()
          .map(weather -> WeatherMapper.toDto(weather, weatherAPILocation))
          .ifPresent(result::add);
    }
    return result;
  }

  public WeatherAPILocation getWeatherAPILocation(double longitude, double latitude) {
    List<String> locationNames = kakaoApiService.getLocationNames(latitude, longitude);
    LccGridConverter.XY gridXY = LccGridConverter.toGrid(latitude, longitude);

    return new WeatherAPILocation(
        latitude,
        longitude,
        gridXY.x,
        gridXY.y,
        locationNames
    );

  }


}
