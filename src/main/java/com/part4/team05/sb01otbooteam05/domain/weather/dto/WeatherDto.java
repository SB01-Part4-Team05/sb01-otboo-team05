package com.part4.team05.sb01otbooteam05.domain.weather.dto;

import com.part4.team05.sb01otbooteam05.domain.weather.entity.SkyStatusType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record WeatherDto(
    @NotNull
    UUID id,

    @NotNull
    LocalDateTime forecastedAt,

    @NotNull
    LocalDateTime forecastAt,

    WeatherAPILocation location,

    @NotNull
    SkyStatusType skyStatus,

    PrecipitationDto precipitation,

    HumidityDto humidity,

    TemperatureDto temperature,

    WindSpeedDto windSpeed
) {

}