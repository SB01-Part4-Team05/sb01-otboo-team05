package com.part4.team05.sb01otbooteam05.domain.weather.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class BaseTimeUtils {

  // 기상청 예보 발표 시간
  private static final List<LocalTime> BASE_TIMES = List.of(
      LocalTime.of(2, 0),
      LocalTime.of(5, 0),
      LocalTime.of(8, 0),
      LocalTime.of(11, 0),
      LocalTime.of(14, 0),
      LocalTime.of(17, 0),
      LocalTime.of(20, 0),
      LocalTime.of(23, 0)
  );

  // baseDateTime 계산
  public static Pair<LocalDate, String> getLatestBaseDateTime() {
    LocalDate nowDate = LocalDate.now();
    LocalTime nowTime = LocalTime.now();

    LocalTime latestBaseTime = BASE_TIMES.stream()
        .filter(time -> !nowTime.isBefore(time))
        .reduce((first, second) -> second)
        .orElse(null);

    if (latestBaseTime != null) {
      return Pair.of(nowDate, formatTime(latestBaseTime));
    }

    return Pair.of(nowDate.minusDays(1), "2300");
  }

  private static String formatTime(LocalTime time) {
    return time.format(DateTimeFormatter.ofPattern("HHmm"));
  }

  public static LocalDateTime toDateTime(String date, String time) {
    return LocalDateTime.of(
        LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd")),
        LocalTime.parse(time, DateTimeFormatter.ofPattern("HHmm"))
    );
  }

}
