package com.part4.team05.sb01otbooteam05.domain.feedComment.event;

import java.util.UUID;

import com.part4.team05.sb01otbooteam05.domain.feed.dto.AuthorDto;

import lombok.Getter;

@Getter
public class CommentCreatedEvent {
	private final UUID feedId;
	private final AuthorDto author;
	private final String content;

	public CommentCreatedEvent(UUID feedId, AuthorDto author, String content) {
		this.feedId = feedId;
		this.author = author;
		this.content = content;
	}
}
