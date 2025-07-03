package com.part4.team05.sb01otbooteam05.domain.weather.dto;

import com.part4.team05.sb01otbooteam05.domain.weather.entity.SkyStatusType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record WeatherSummaryDto(
    @NotNull
    UUID weatherId,

    @NotNull
    SkyStatusType skyStatus,

    PrecipitationDto precipitation,

    TemperatureDto temperature
) {

}
