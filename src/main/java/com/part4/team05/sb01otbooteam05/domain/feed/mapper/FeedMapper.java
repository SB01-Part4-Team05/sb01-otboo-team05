package com.part4.team05.sb01otbooteam05.domain.feed.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.part4.team05.sb01otbooteam05.domain.feed.dto.AuthorDto;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDto;
import com.part4.team05.sb01otbooteam05.domain.feed.entity.Feed;
import com.part4.team05.sb01otbooteam05.domain.ootd.dto.OotdDto;
import com.part4.team05.sb01otbooteam05.domain.ootd.entity.Ootd;
import com.part4.team05.sb01otbooteam05.domain.ootd.mapper.OotdMapper;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.weather.Mapper.WeatherMapper;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherDto;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;

@Mapper(componentModel = "spring", uses = {WeatherMapper.class, OotdMapper.class})
public interface FeedMapper {

	List<FeedDto> toDtoList(List<Feed> feeds);

	AuthorDto toDto(User user);

	WeatherDto toDto(Weather weather);

	OotdDto toDto(Ootd ootd);

	List<OotdDto> toOotdDtoList(List<Ootd> ootds);

	default FeedDto toDto(Feed feed, Long likeCount, Integer commentCount, Boolean likedByMe) {
		return new FeedDto(
			feed.getId(),
			feed.getCreatedAt(),
			feed.getUpdatedAt(),
			toDto(feed.getAuthor()),
			toDto(feed.getWeather()),
			toOotdDtoList(feed.getOotds()),
			feed.getContent(),
			likeCount,
			commentCount,
			likedByMe
		);
	}
}
