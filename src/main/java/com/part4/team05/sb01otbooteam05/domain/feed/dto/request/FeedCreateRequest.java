package com.part4.team05.sb01otbooteam05.domain.feed.dto.request;

import java.util.List;
import java.util.UUID;

public record FeedCreateRequest(
	UUID authorId,
	UUID weatherId,
	List<UUID> clothesIds,
	String content
) {
}
