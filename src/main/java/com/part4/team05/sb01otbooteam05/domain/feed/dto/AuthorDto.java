package com.part4.team05.sb01otbooteam05.domain.feed.dto;

import java.util.UUID;

public record AuthorDto(
	UUID userId,
	String name,
	String profileImageUrl
) {
}
