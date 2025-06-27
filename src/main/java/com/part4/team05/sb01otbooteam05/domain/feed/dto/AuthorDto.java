package com.part4.team05.sb01otbooteam05.domain.feed.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AuthorDto(
	@NotNull
	UUID userId,

	@NotBlank
	@Size(min = 1, max = 20) //User 객체의 length 값을 따름
	String name,

	String profileImageUrl
) {
}
