package com.part4.team05.sb01otbooteam05.domain.weather.service;

import static java.lang.Double.*;

import com.part4.team05.sb01otbooteam05.domain.user.service.KakaoApiService;
import com.part4.team05.sb01otbooteam05.domain.user.util.LccGridConverter;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherAPILocation;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherDto;
import com.part4.team05.sb01otbooteam05.domain.weather.exception.InvalidDataException;
import com.part4.team05.sb01otbooteam05.domain.weather.exception.WeatherBatchException;
import com.part4.team05.sb01otbooteam05.domain.weather.mapper.WeatherMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
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
  private final JobLauncher jobLauncher;
  private final Job singleLocationWeatherJob;

  @Transactional
  public List<Weather> generateWeather(int x, int y) {
    //예보 위치 x,y 값 받기
    ParsedForecastDto parsedForecastDto = weatherApiClient.fetchForecast(x, y);
    return parsedForecastDtoToWeathers(parsedForecastDto, x, y);
  }

  // 기상청 API 응답값 정제
  public List<Weather> parsedForecastDtoToWeathers(ParsedForecastDto parsedForecastDto, int x,
      int y) {

    //예보 등록 기준 시간
    LocalDateTime forecastedAt = parsedForecastDto.forecastedDateTime();

    //예보 정보 담기
    Map<LocalDateTime, Map<String, String>> forecastMap = parsedForecastDto.forecastMap();

    // 날짜별 모든 시간 TMP(온도) 수집
    Map<LocalDate, Double> minTemps = getMinTemps(forecastMap);
    Map<LocalDate, Double> maxTemps = getMaxTemps(forecastMap);

    // forecastMap에 없는 전날 forecastAt 모으기 (DB 조회용)
    Set<LocalDateTime> missingYesterdays = forecastMap.keySet().stream()
        .map(dt -> dt.minusDays(1))
        .filter(dt -> !forecastMap.containsKey(dt))
        .collect(Collectors.toSet());

    // 필요한 전날 데이터들 DB에서 한 번에 조회
    List<Weather> yesterdayWeathers = weatherRepository.findByLocationXAndLocationYAndForecastAtIn(
        x, y, missingYesterdays);

    // DB 결과를 Map으로 빠르게 조회
    Map<LocalDateTime, Weather> yesterdayMap = yesterdayWeathers.stream()
        .collect(Collectors.toMap(Weather::getForecastAt, Function.identity()));

    // 날씨 리스트 생성
    List<Weather> weathers = new ArrayList<>();

    // 시간별 날씨 엔티티 생성 작업
    for (Map.Entry<LocalDateTime, Map<String, String>> entry : forecastMap.entrySet()) {
      LocalDateTime forecastAt = entry.getKey();
      Map<String, String> values = entry.getValue();

      String tmpStr = values.get("TMP");
      String rehStr = values.get("REH");

      if (tmpStr == null || tmpStr.isBlank() || rehStr == null || rehStr.isBlank()) {
        log.warn("기온 or 습도 값이 null 또는 빈 값임: TMP={}, REH={}, forecastAt={}", tmpStr, rehStr,
            forecastAt);
        continue; // 해당 데이터는 스킵
      }

      double tmp = parseDouble(tmpStr);
      double reh = parseDouble(rehStr);

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
        Weather yesterday = yesterdayMap.get(yesterdayForecastAt);
        if (yesterday != null) {
          tmpDiff = tmp - yesterday.getTemperatureCurrent();
          rehDiff = reh - yesterday.getHumidityCurrent();
        }
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
          throw new InvalidDataException("파싱 실패");
        }
      }
    }

    return tmpPerDay;
  }

  // x, y, forecastedAt 값으로 날씨 데이터 유무 확인
  public boolean existWeather(int x, int y, LocalDateTime forecastedAt) {
    return weatherRepository.existsByLocationXAndLocationYAndForecastedAt(x, y, forecastedAt);
  }

  @Transactional(readOnly = true)
  public Weather getWeatherEntityByIdOrThrow(UUID weatherId) {
    return weatherRepository.findById(weatherId)
        .orElseThrow(() -> WeatherNotFoundException.withId(weatherId));
  }

  @Transactional(readOnly = true)
  public List<WeatherDto> getWeathers(double longitude, double latitude) {

    WeatherAPILocation weatherAPILocation = getWeatherAPILocation(longitude, latitude);
    LocalDateTime now = LocalDateTime.now();
    LocalTime requestedTime = now.toLocalTime().truncatedTo(ChronoUnit.HOURS);
    List<LocalDateTime> targetForecastAtList = new ArrayList<>();

    // 기상청 날씨 정보가 3일 뒤부터는 매 시간마다 정보를 주지 않아 00시로 고정
    for (int i = 0; i <= 4; i++) {
      LocalDate targetDate = now.toLocalDate().plusDays(i);
      LocalTime targetTime = i < 2 ? requestedTime : LocalTime.MIDNIGHT;
      LocalDateTime targetForecastAt = LocalDateTime.of(targetDate, targetTime);
      targetForecastAtList.add(targetForecastAt);
      log.info("요청 기준 forecastAt (targetForecastAt): {}, x = {}, y = {}", targetForecastAt,
          weatherAPILocation.x(), weatherAPILocation.y());
    }

    List<Weather> weathers = weatherRepository.findByLocationXAndLocationYAndForecastAtIn(
        weatherAPILocation.x(), weatherAPILocation.y(), targetForecastAtList);

    Map<LocalDateTime, Weather> weatherMap = weathers.stream()
        .collect(Collectors.toMap(Weather::getForecastAt, Function.identity()));

    List<WeatherDto> result = targetForecastAtList.stream()
        .map(weatherMap::get)
        .filter(Objects::nonNull)
        .map(weather -> WeatherMapper.toDto(weather, weatherAPILocation))
        .collect(Collectors.toList());

    return result;
  }

  // 날씨 단건 조회 시 날씨 데이터 생성 ( singleLocationWeatherJob 실행 )
  public WeatherAPILocation getWeatherAPILocationAndGenerateWeather(double longitude,
      double latitude) {
    WeatherAPILocation weatherAPILocation = getWeatherAPILocation(longitude, latitude);
    int x = weatherAPILocation.x();
    int y = weatherAPILocation.y();
    boolean exists = weatherRepository.existsByLocationXAndLocationY(x, y);
    if (!exists) {
      try {
        JobParameters parameters = new JobParametersBuilder()
            .addString("x", String.valueOf(x))
            .addString("y", String.valueOf(y))
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();
        log.info("단일 위치 배치 실행: x={}, y={}", x, y);
        jobLauncher.run(singleLocationWeatherJob, parameters);
      } catch (JobExecutionException e) {
        log.error("단일 위치 날씨 배치 실행 실패: x={}, y={}", x, y);
        throw new WeatherBatchException();
      } catch (Exception e) {
        throw new WeatherBatchException();
      }
    } else {
      log.info("날씨 데이터 존재: x={}, y={}", x, y);
    }
    return weatherAPILocation;
  }

  // 위도 경도 값으로 WeatherAPILocation 생성
  public WeatherAPILocation getWeatherAPILocation(double longitude, double latitude) {
    List<String> locationNames = kakaoApiService.getLocationNames(latitude, longitude);
    LccGridConverter.XY gridXY = LccGridConverter.toGrid(latitude, longitude);

    log.info("WeatherAPILocation 생성 : longitude = {}, latitude = {}", longitude, latitude);
    return new WeatherAPILocation(
        latitude,
        longitude,
        gridXY.x,
        gridXY.y,
        locationNames
    );

  }
}
