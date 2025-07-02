package com.part4.team05.sb01otbooteam05.domain.weather.service;

import static java.lang.Double.parseDouble;

import com.part4.team05.sb01otbooteam05.domain.weather.WeatherApiClient;
import com.part4.team05.sb01otbooteam05.domain.weather.WeatherCategoryMapper;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.HumidityDto;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.ParsedForecastDto;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.PrecipitationDto;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.TemperatureDto;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherAPILocation;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherDto;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WindSpeedDto;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.SkyStatusType;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import com.part4.team05.sb01otbooteam05.domain.weather.repository.WeatherRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

  private final WeatherApiClient weatherApiClient;
  private final WeatherRepository weatherRepository;

  // 기상청 API 응답값 저장
  public void saveWeather(WeatherAPILocation weatherAPILocation) {
    //예보 위치 x,y 값 받기
    ParsedForecastDto parsedForecastDto = weatherApiClient.fetchForecast(weatherAPILocation.x(), weatherAPILocation.y());

    //예보 등록 기준 시간
    LocalDateTime forecastedAt = parsedForecastDto.forecastedDateTime();

    //예보 정보 담기
    Map<LocalDateTime, Map<String, String>> forecastMap = parsedForecastDto.forecastMap();

    // 날짜별 모든 시간 TMP(온도) 수집
    Map<LocalDate, List<Double>> tmpPerDay = new HashMap<>();
    for (Map.Entry<LocalDateTime, Map<String, String>> entry : forecastMap.entrySet()) {
      String tmp = entry.getValue().get("TMP");
      if(tmp != null) {
        double value = parseDouble(tmp);
        tmpPerDay.computeIfAbsent(entry.getKey().toLocalDate(), k -> new ArrayList<>()).add(value);
      }
    }

    // 날짜 min/max 계산
    Map<LocalDate, Double> minTempMap = new HashMap<>();
    Map<LocalDate, Double> maxTempMap = new HashMap<>();
    tmpPerDay.forEach((date, temps) -> {
      minTempMap.put(date, temps.stream().min(Double::compareTo).orElse(null));
      maxTempMap.put(date, temps.stream().max(Double::compareTo).orElse(null));
    });

    // 결과 리스트 생성
    List<WeatherDto> result = new ArrayList<>();

    // 결과 리스트 작업
    for (Map.Entry<LocalDateTime, Map<String, String>> entry : forecastMap.entrySet()) {
      LocalDateTime forecastAt = entry.getKey();
      Map<String, String> values = entry.getValue();

      double tmp = parseDouble(values.get("TMP"));
      double reh = parseDouble(values.get("TMP"));

      // 온도, 습도 전날 비교 계산
      // todo 예보 등록 기준일은 db에 있는 정보를 가져와서 비교해야 할 것 같음.
      Map<String, String> yesterdayData = forecastMap.get(forecastAt.minusDays(1));
      Double tmpDiff = (yesterdayData != null && yesterdayData.containsKey("TMP"))
          ? tmp - parseDouble(yesterdayData.get("TMP"))
          : null;
      Double rehDiff = (yesterdayData != null && yesterdayData.containsKey("REH"))
          ? reh - parseDouble(yesterdayData.get("REH"))
          : null;

      // TemperatureDto 생성
      TemperatureDto temperatureDto = new TemperatureDto(
          tmp,
          tmpDiff,
          minTempMap.get(forecastAt.toLocalDate()),
          maxTempMap.get(forecastAt.toLocalDate())
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
          parseDouble(values.get("PCP")), //todo 강수량이 나와야함. ex) 강수없음 -> 0.0
          parseDouble(values.get("POP"))
      );

      SkyStatusType skyStatusType = WeatherCategoryMapper.toSkyStatusType(values.get("SKY"));

      // Weather 엔티티 생성
      Weather weather = Weather.builder()
          .locationX(weatherAPILocation.x())
          .locationY(weatherAPILocation.y())
          .forecastedAt(forecastedAt)
          .forecastAt(forecastAt)
          .skyStatusType(skyStatusType)
          .precipitationType(precipitationDto.type())
          .precipitationAmount(precipitationDto.amount())
          .precipitationProbability(precipitationDto.probability())
          .humidityCurrent(humidityDto.current())
          .humidityComparedToDayBefore(humidityDto.comparedToDayBefore())
          .temperatureCurrent(temperatureDto.current())
          .temperatureComparedToDayBefore(temperatureDto.comparedToDayBefore())
          .temperatureMin(temperatureDto.min())
          .temperatureMax(temperatureDto.max())
          .windSpeed(windSpeedDto.speed())
          .windSpeedAsWord(windSpeedDto.asWord())
          .build();

      weatherRepository.save(weather);

      //WeatherDto 생성
      WeatherDto weatherDto = new WeatherDto(
          weather.getId(),
          forecastedAt,
          forecastAt,
          weatherAPILocation,
          weather.getSkyStatusType(),
          precipitationDto,
          humidityDto,
          temperatureDto,
          windSpeedDto
      );

      result.add(weatherDto);
    }
  }
}
