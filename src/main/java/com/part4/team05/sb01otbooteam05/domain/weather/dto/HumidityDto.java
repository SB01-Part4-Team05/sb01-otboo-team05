package com.part4.team05.sb01otbooteam05.domain.weather.dto;

import jakarta.validation.constraints.NotNull;

public record HumidityDto(
    @NotNull
    Double current,

    Double comparedToDayBefore
) {

}
