package com.part4.team05.sb01otbooteam05.domain.feedComment.dto;

import java.util.List;

import org.springframework.data.domain.Sort;

import jakarta.validation.constraints.NotNull;

public record CommentsPageResponse(

	@NotNull
	List<CommentDto> comments,

	String nextCursor,

	String nextIdAfter,

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
