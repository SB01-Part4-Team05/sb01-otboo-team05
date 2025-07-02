package com.part4.team05.sb01otbooteam05.domain.weather.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ParsedForecastDto(
    LocalDateTime forecastedDateTime,

    Map<LocalDateTime, Map<String, String>> forecastMap
) {

}
