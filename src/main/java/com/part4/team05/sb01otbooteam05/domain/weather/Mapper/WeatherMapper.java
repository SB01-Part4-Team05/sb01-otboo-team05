package com.part4.team05.sb01otbooteam05.domain.weather.Mapper;

import org.mapstruct.Mapper;

import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherDto;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;

@Mapper(componentModel = "spring")
public interface WeatherMapper {
	WeatherDto toDto(Weather weather);

}
