package com.part4.team05.sb01otbooteam05.domain.feed.mapper;

import com.part4.team05.sb01otbooteam05.domain.ootd.mapper.OotdMapper;
import java.util.List;

import org.mapstruct.Mapper;

import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDto;
import com.part4.team05.sb01otbooteam05.domain.feed.entity.Feed;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = OotdMapper.class)
public interface FeedMapper {

	@Mapping(target = "likeCount", ignore = true)
	@Mapping(target = "commentCount", ignore = true)
	@Mapping(target = "likedByMe",ignore = true)
	FeedDto toDto(Feed feed);

	List<FeedDto> toDtoList(List<Feed> feeds);

	Feed toEntity(FeedDto feedDto);

	List<Feed> toEntityList(List<FeedDto> feedDtos);

}
