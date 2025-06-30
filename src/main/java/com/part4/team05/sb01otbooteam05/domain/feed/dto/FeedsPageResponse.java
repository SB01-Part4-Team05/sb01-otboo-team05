package com.part4.team05.sb01otbooteam05.domain.feed.dto;

import java.util.List;

import org.springframework.data.domain.Sort;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record FeedsPageResponse(

	@NotNull
	@Valid
	List<FeedDto> data,

	String nextCursor,

	String IdAfter,

	@NotNull
	Boolean hasNext,

	@NotNull
	Long totalCount,

	@NotNull
	String sortBy,

	@NotNull
	Sort.Direction direction
) {
}
