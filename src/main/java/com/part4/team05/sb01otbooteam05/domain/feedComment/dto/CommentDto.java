package com.part4.team05.sb01otbooteam05.domain.feedComment.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.part4.team05.sb01otbooteam05.domain.feed.dto.AuthorDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentDto(

	@NotNull
	UUID id,

	@NotNull
	LocalDateTime createdAt,

	@NotNull
	UUID feedId,

	@NotNull
	AuthorDto author,

	@NotBlank
	String content
) {
}
