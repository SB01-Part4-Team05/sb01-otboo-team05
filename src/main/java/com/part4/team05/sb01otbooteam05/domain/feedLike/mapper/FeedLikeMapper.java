package com.part4.team05.sb01otbooteam05.domain.feedLike.mapper;

import static com.part4.team05.sb01otbooteam05.domain.weather.mapper.WeatherMapper.toHumidityDto;
import static com.part4.team05.sb01otbooteam05.domain.weather.mapper.WeatherMapper.toPrecipitationDto;
import static com.part4.team05.sb01otbooteam05.domain.weather.mapper.WeatherMapper.toTemperatureDto;
import static com.part4.team05.sb01otbooteam05.domain.weather.mapper.WeatherMapper.toWindSpeedDto;

import com.part4.team05.sb01otbooteam05.domain.ootd.mapper.OotdMapper;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherAPILocation;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherDto;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WindSpeedDto;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import com.part4.team05.sb01otbooteam05.domain.weather.mapper.WeatherCategoryMapper;
import java.util.Collections;
import org.mapstruct.Mapper;

import com.part4.team05.sb01otbooteam05.domain.feedLike.dto.FeedLikeDto;
import com.part4.team05.sb01otbooteam05.domain.feedLike.entity.FeedLike;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = OotdMapper.class)
public interface FeedLikeMapper {

	@Mapping(source = "feed.weather", target = "feed.weather",qualifiedByName = "weatherToDto")
	FeedLikeDto toDto(FeedLike feedLike);


	@Mapping(source = "feed.weather.windSpeed", target = "feed.weather.windSpeed", qualifiedByName = "windSpeedDtoToDouble")
	FeedLike toEntity(FeedLikeDto feedLikeDto);


	@Named("weatherToDto")
	default WeatherDto toWeatherDto(Weather weather) {
		return new WeatherDto(
				weather.getId(),
				weather.getForecastedAt(),
				weather.getForecastAt(),
				new WeatherAPILocation(Double.valueOf(weather.getLocationX()),Double.valueOf(weather.getLocationY()),
						weather.getLocationX(),weather.getLocationY(), Collections.emptyList()),
				weather.getSkyStatusType(),
				toPrecipitationDto(weather),
				toHumidityDto(weather),
				toTemperatureDto(weather),
				toWindSpeedDto(weather)
		);
	}

	@Named("windSpeedDtoToDouble")
	default Double map(WindSpeedDto dto) {
		return dto != null ? dto.speed() : null;
	}
}
