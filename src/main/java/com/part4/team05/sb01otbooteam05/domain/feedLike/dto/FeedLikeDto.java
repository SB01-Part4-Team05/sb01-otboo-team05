package com.part4.team05.sb01otbooteam05.domain.feedLike.dto;

import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDto;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;

public record FeedLikeDto(
	FeedDto feed,
	User author
) {
}
