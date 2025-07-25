package com.part4.team05.sb01otbooteam05.domain.weather.service;

import com.part4.team05.sb01otbooteam05.domain.notification.entity.NotificationLevel;
import com.part4.team05.sb01otbooteam05.domain.notification.service.NotificationService;
import com.part4.team05.sb01otbooteam05.domain.user.service.UserService;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import com.part4.team05.sb01otbooteam05.domain.weather.repository.WeatherRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherNotificationService {

  private final UserService userService;
  private final WeatherRepository weatherRepository;
  private final NotificationService notificationService;

  public record Location(int x, int y) {}

  public enum WeatherChangedType {
    TEMP_CHANGE,
    RAIN_SNOW_STARTED
  }

  public void generateNotifications(int x, int y) {
    LocalDateTime now = LocalDateTime.now()
        .withMinute(0)
        .withSecond(0)
        .withNano(0);

    // 현재 시간의 한시간 뒤 날씨 데이터 기준
    LocalDateTime forecastAt = now.plusHours(1);

    List<Weather> weatherList = weatherRepository.findByForecastAtAndLocationXAndLocationY(forecastAt, x, y);

    // 날씨 데이터 forecastedAt이 한 가지만 있을 때는 빈값 반환
    if (weatherList.size() < 2) return;

    Map<Location, Pair<Weather, Weather>> comparisonMap = getComparisonMap(weatherList);

    for (Map.Entry<Location, Pair<Weather, Weather>> entry : comparisonMap.entrySet()) {
      Location location = entry.getKey();
      Weather latest = entry.getValue().getLeft();
      Weather previous = entry.getValue().getRight();

      List<WeatherChangedType> changedTypes = detectChangedTypes(latest, previous);

      for (WeatherChangedType type : changedTypes) {
        String message = buildMessage(type, latest, previous);
        List<UUID> userIds = userService.findUserIdsByLocation(location.x(), location.y());

        for (UUID userId : userIds) {
          notificationService.createAndSendNotification(
              userId,
              "날씨 변화 알림",
              message,
              NotificationLevel.WARNING
          );
          log.info("알림 전송: userId={}, x={}, y={}, message={}",
              userId, location.x(), location.y(), message);
        }
      }
    }
  }

  // 지역별로 최신/직전 forecastedAt 데이터 반환
  private Map<Location, Pair<Weather, Weather>> getComparisonMap(List<Weather> weatherList) {
    Map<Location, List<Weather>> grouped = weatherList.stream()
        .collect(Collectors.groupingBy(weather ->
            new Location(weather.getLocationX(), weather.getLocationY())
        ));

    Map<Location, Pair<Weather, Weather>> result = new HashMap<>();

    for (Map.Entry<Location, List<Weather>> entry : grouped.entrySet()) {
      List<Weather> sorted = entry.getValue().stream()
          .sorted(Comparator.comparing(Weather::getForecastedAt).reversed())
          .limit(2)
          .toList();

      if (sorted.size() == 2) {
        result.put(entry.getKey(), Pair.of(sorted.get(0), sorted.get(1)));
      }
    }

    return result;
  }

  // 날씨 알림 조건 확인
  List<WeatherChangedType> detectChangedTypes(Weather latest, Weather previous) {
    List<WeatherChangedType> changes = new ArrayList<>();

    Double latestTemp = latest.getTemperatureCurrent();
    Double previousTemp = previous.getTemperatureCurrent();

    // 온도가 5도 이상 차이가 날 경우
    if (latestTemp != null && previousTemp != null &&
        Math.abs(latestTemp - previousTemp) >= 5.0) {
      changes.add(WeatherChangedType.TEMP_CHANGE);
    }

    // 날씨가 NONE 에서 RAIN, SNOW, RAIN_SNOW, SHOWER 로 비뀔 경우
    if (!previous.getPrecipitationType().isRainOrSnow()
        && latest.getPrecipitationType().isRainOrSnow()) {
      changes.add(WeatherChangedType.RAIN_SNOW_STARTED);
    }

    return changes;
  }

  private String buildMessage(WeatherChangedType type, Weather latest, Weather previous) {
    return switch (type) {
      case RAIN_SNOW_STARTED -> "새로운 비/눈 예보가 추가되었습니다.";
      case TEMP_CHANGE -> {
        double diff = latest.getTemperatureCurrent() - previous.getTemperatureCurrent();
        String direction = diff > 0 ? "올라" : "내려";
        yield String.format("기온이 %.1f도 %s갔습니다.", Math.abs(diff), direction);
      }
    };
  }
}
