package com.part4.team05.sb01otbooteam05.domain.feedComment.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.part4.team05.sb01otbooteam05.domain.feed.dto.AuthorDto;

public record CommentDto(
	UUID id,
	LocalDateTime createdAt,
	UUID feedId,
	AuthorDto author,
	String content
) {
}
