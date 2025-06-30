package com.part4.team05.sb01otbooteam05.domain.feed.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDto;
import com.part4.team05.sb01otbooteam05.domain.feed.entity.Feed;

@Mapper(componentModel = "spring")
public interface FeedMapper {
	FeedDto toDto(Feed feed);

	List<FeedDto> toDtoList(List<Feed> feeds);

	Feed toEntity(FeedDto feedDto);

	List<Feed> toEntityList(List<FeedDto> feedDtos);

}
