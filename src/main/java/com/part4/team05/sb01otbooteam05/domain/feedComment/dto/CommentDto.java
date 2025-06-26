package com.part4.team05.sb01otbooteam05.domain.feedComment.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentDto(
	// todo userDto 만들어지면 수정
	UUID id,
	Instant createdAt,
	UUID feedId,
	//UserDto author,
	String content
) {
}
