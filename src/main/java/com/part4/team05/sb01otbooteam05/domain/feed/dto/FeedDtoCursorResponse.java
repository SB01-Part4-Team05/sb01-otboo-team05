package com.part4.team05.sb01otbooteam05.domain.feed.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.hibernate.query.SortDirection;

import java.util.List;

public record FeedDtoCursorResponse(

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
	SortDirection direction
) {
}
