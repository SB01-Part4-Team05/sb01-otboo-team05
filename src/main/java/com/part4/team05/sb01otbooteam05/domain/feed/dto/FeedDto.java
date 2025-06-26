package com.part4.team05.sb01otbooteam05.domain.feed.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.part4.team05.sb01otbooteam05.domain.ootd.dto.OotdDto;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherDto;

public record FeedDto (
	// todo userDto 만들어지면 수정
	UUID id,
	Instant createdAt,
	Instant updatedAt,
	// UserDto author,
	WeatherDto weather,
	List<OotdDto> ootds,
	String content,
	Long likeCount,
	Integer commentCount,
	Boolean likedByMe
	) {
}
