package com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request;

import java.util.UUID;

public record FindCommentsRequest(
	UUID feedId,
	String cursor,
	UUID idAfter,
	Integer limit
) {
}
