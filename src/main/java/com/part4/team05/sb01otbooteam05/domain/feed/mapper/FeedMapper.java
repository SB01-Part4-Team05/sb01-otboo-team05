package com.part4.team05.sb01otbooteam05.domain.feed.mapper;

import static com.part4.team05.sb01otbooteam05.domain.weather.mapper.WeatherMapper.toHumidityDto;
import static com.part4.team05.sb01otbooteam05.domain.weather.mapper.WeatherMapper.toPrecipitationDto;
import static com.part4.team05.sb01otbooteam05.domain.weather.mapper.WeatherMapper.toTemperatureDto;
import static com.part4.team05.sb01otbooteam05.domain.weather.mapper.WeatherMapper.toWindSpeedDto;

import com.part4.team05.sb01otbooteam05.domain.ootd.mapper.OotdMapper;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherAPILocation;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherDto;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WindSpeedDto;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import java.util.Collections;
import java.util.List;

import org.mapstruct.Mapper;

import com.part4.team05.sb01otbooteam05.domain.feed.dto.AuthorDto;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDto;
import com.part4.team05.sb01otbooteam05.domain.feed.entity.Feed;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = OotdMapper.class)
public interface FeedMapper {

	@Mapping(target = "likeCount", ignore = true)
	@Mapping(target = "commentCount", ignore = true)
	@Mapping(target = "likedByMe",ignore = true)
	@Mapping(source = "weather", target = "weather",qualifiedByName = "weatherToDto")
	FeedDto toDto(Feed feed);

	List<FeedDto> toDtoList(List<Feed> feeds);

	@Mapping(source = "weather.windSpeed", target = "weather.windSpeed", qualifiedByName = "windSpeedDtoToDouble")
	Feed toEntity(FeedDto feedDto);

	OotdDto toDto(Ootd ootd);

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
