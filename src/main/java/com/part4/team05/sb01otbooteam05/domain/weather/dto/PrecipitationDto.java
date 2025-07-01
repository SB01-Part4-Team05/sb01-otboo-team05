package com.part4.team05.sb01otbooteam05.domain.weather.dto;


import com.part4.team05.sb01otbooteam05.domain.weather.entity.PrecipitationType;
import jakarta.validation.constraints.NotNull;

public record PrecipitationDto(
    @NotNull
    PrecipitationType type,

    @NotNull
    Double amount,

    @NotNull
    Double probability
) {

}
