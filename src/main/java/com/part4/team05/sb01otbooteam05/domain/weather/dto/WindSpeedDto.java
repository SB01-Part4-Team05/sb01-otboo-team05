package com.part4.team05.sb01otbooteam05.domain.weather.dto;

import com.part4.team05.sb01otbooteam05.domain.weather.entity.WindSpeedAsWord;
import jakarta.validation.constraints.NotNull;

public record WindSpeedDto(
    @NotNull
    Double speed,

    @NotNull
    WindSpeedAsWord asWord
) {

}
