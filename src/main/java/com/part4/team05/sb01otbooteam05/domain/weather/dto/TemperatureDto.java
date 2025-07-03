package com.part4.team05.sb01otbooteam05.domain.weather.dto;

import jakarta.validation.constraints.NotNull;

public record TemperatureDto(
    @NotNull
    Double current,

    Double comparedToDayBefore,

    @NotNull
    Double min,

    @NotNull
    Double max
) {

}
