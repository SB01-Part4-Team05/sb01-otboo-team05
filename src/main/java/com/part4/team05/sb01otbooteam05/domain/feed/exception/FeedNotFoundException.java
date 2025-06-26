package com.part4.team05.sb01otbooteam05.domain.feed.exception;

import java.util.UUID;

import com.part4.team05.sb01otbooteam05.exception.ErrorCode;

public class FeedNotFoundException extends FeedException {
	public FeedNotFoundException() {
		super(ErrorCode.FEED_NOT_FOUND);
	}

	public FeedNotFoundException withId(UUID id) {
		FeedNotFoundException exception = new FeedNotFoundException();
		exception.addDetail("id", id);
		return exception;
	}
}
