package com.part4.team05.sb01otbooteam05.domain.feedComment.dto;

import java.util.List;

import org.springframework.data.domain.Sort;

public record CommentsPageResponse(
	List<CommentDto> comments,
	String nextCursor,
	String nextIdAfter,
	Boolean hasNext,
	Long totalCount,
	String sortBy,
	Sort.Direction direction
) {
}
