package com.part4.team05.sb01otbooteam05.domain.feed.mapper;

import com.part4.team05.sb01otbooteam05.domain.feed.dto.AuthorDto;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDto;
import com.part4.team05.sb01otbooteam05.domain.feed.entity.Feed;
import com.part4.team05.sb01otbooteam05.domain.feedComment.repository.FeedCommentRepository;
import com.part4.team05.sb01otbooteam05.domain.feedLike.repository.FeedLikeRepository;
import com.part4.team05.sb01otbooteam05.domain.ootd.dto.OotdDto;
import com.part4.team05.sb01otbooteam05.domain.ootd.entity.Ootd;
import com.part4.team05.sb01otbooteam05.domain.ootd.mapper.OotdMapper;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherAPILocation;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherDto;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static com.part4.team05.sb01otbooteam05.domain.weather.Mapper.WeatherMapper.*;


@Component
@RequiredArgsConstructor
public class FeedMapper {

	private final FeedLikeRepository feedLikeRepository;
	private final FeedCommentRepository feedCommentRepository;
	private final OotdMapper ootdMapper;


	public List<FeedDto> toFeedDtoList(List<Feed> feeds, User user){
		return feeds.stream().map(
				feed -> toFeedDto(feed, countFeedLikeByFeed(feed), countCommentByFeed(feed), isLikedByMe(feed, user)
				)).toList();
	}

	public AuthorDto toAuthorDto(User user){
		return new AuthorDto(user.getId(), user.getName(), user.getProfileImageUrl());
	}

	public OotdDto toOotdDto(Ootd ootd){
		return ootdMapper.toOotdDto(ootd);
	}

	public List<OotdDto> toOotdDtoList(List<Ootd> ootds) {
		return ootds.stream().map(ootd->toOotdDto(ootd)).toList();
	}

	public FeedDto toFeedDto(Feed feed, Long likeCount, Integer commentCount, Boolean likedByMe) {
		return new FeedDto(
				feed.getId(),
				feed.getCreatedAt(),
				feed.getUpdatedAt(),
				toAuthorDto(feed.getAuthor()),
				toWeatherDto(feed.getWeather()),
				toOotdDtoList(feed.getOotds()),
				feed.getContent(),
				likeCount,
				commentCount,
				likedByMe
		);
	}

	public FeedDto toFeedDto(Feed feed, User user) {
		return new FeedDto(
				feed.getId(),
				feed.getCreatedAt(),
				feed.getUpdatedAt(),
				toAuthorDto(feed.getAuthor()),
				toWeatherDto(feed.getWeather()),
				toOotdDtoList(feed.getOotds()),
				feed.getContent(),
				countFeedLikeByFeed(feed),
				countCommentByFeed(feed),
				isLikedByMe(feed, user)
		);
	}

	/*@Mapping(source = "weather.windSpeed", target = "weather.windSpeed", qualifiedByName = "windSpeedDtoToDouble")
	Feed toEntity(FeedDto feedDto);*/



	WeatherDto toWeatherDto(Weather weather) {
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



	/*@Named("windSpeedDtoToDouble")
	default Double map(WindSpeedDto dto) {
		return dto != null ? dto.speed() : null;
	}*/

	private Long countFeedLikeByFeed(Feed feed) {
		return feedLikeRepository.countByFeed(feed);
	}

	private Integer countCommentByFeed(Feed feed) {
		return feedCommentRepository.countByFeed(feed);
	}

	private boolean isLikedByMe(Feed feed, User user){
		return feedLikeRepository.findByFeedAndAuthor(feed, user).isPresent();
	}
}
