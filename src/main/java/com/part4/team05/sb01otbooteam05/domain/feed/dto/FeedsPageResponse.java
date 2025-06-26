package com.part4.team05.sb01otbooteam05.domain.feed.dto;

import java.util.List;

import org.springframework.data.domain.Sort;

public record FeedsPageResponse(
	List<FeedDto> data,
	String nextCursor,
	String IdAfter,
	Boolean hasNext,
	Long totalCount,
	String sortBy,
	Sort.Direction direction
) {
}
