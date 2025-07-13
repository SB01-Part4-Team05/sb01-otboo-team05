package com.part4.team05.sb01otbooteam05.domain.feedLike.mapper;

import static com.part4.team05.sb01otbooteam05.domain.weather.mapper.WeatherMapper.toHumidityDto;
import static com.part4.team05.sb01otbooteam05.domain.weather.mapper.WeatherMapper.toPrecipitationDto;
import static com.part4.team05.sb01otbooteam05.domain.weather.mapper.WeatherMapper.toTemperatureDto;
import static com.part4.team05.sb01otbooteam05.domain.weather.mapper.WeatherMapper.toWindSpeedDto;

import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDto;
import com.part4.team05.sb01otbooteam05.domain.feed.entity.Feed;
import com.part4.team05.sb01otbooteam05.domain.feed.mapper.FeedMapper;
import com.part4.team05.sb01otbooteam05.domain.ootd.mapper.OotdMapper;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherAPILocation;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherDto;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WindSpeedDto;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import com.part4.team05.sb01otbooteam05.domain.weather.mapper.WeatherCategoryMapper;
import java.util.Collections;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;

import com.part4.team05.sb01otbooteam05.domain.feedLike.dto.FeedLikeDto;
import com.part4.team05.sb01otbooteam05.domain.feedLike.entity.FeedLike;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedLikeMapper {

	private final FeedMapper feedMapper;

	public FeedLikeDto toDto(FeedLike feedLike, User user){
		return new FeedLikeDto(feedMapper.toFeedDto(feedLike.getFeed(), user), feedMapper.toAuthorDto(feedLike.getAuthor()));
	}

}
