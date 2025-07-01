package com.part4.team05.sb01otbooteam05.domain.weather.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record WeatherAPILocation(
    @NotNull
    Double latitude,

    @NotNull
    Double longitude,

    @NotNull
    Integer x,

    @NotNull
    Integer y,

    @NotNull
    List<String> locationNames
) {

}
