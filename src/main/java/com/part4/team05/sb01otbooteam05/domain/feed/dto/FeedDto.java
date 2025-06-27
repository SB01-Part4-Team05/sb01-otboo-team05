package com.part4.team05.sb01otbooteam05.domain.feed.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.part4.team05.sb01otbooteam05.domain.ootd.dto.OotdDto;
import com.part4.team05.sb01otbooteam05.domain.user.dto.AuthorDto;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherDto;

public record FeedDto (
	UUID id,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	AuthorDto author,
	WeatherDto weather,
	List<OotdDto> ootds,
	String content,
	Long likeCount,
	Integer commentCount,
	Boolean likedByMe
	) {
}
