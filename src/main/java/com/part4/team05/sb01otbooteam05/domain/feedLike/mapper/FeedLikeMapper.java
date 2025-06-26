package com.part4.team05.sb01otbooteam05.domain.feedLike.mapper;

import org.mapstruct.Mapper;

import com.part4.team05.sb01otbooteam05.domain.feedLike.dto.FeedLikeDto;
import com.part4.team05.sb01otbooteam05.domain.feedLike.entity.FeedLike;

@Mapper(componentModel = "spring")
public interface FeedLikeMapper {
	FeedLikeDto toDto(FeedLike feedLike);

	FeedLike toEntity(FeedLikeDto feedLikeDto);
}
