package com.part4.team05.sb01otbooteam05.domain.feedLike.dto;

import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDto;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.AuthorDto;

//todo 좋아요 객체를 반환할일이 있을까..? 없다면 그냥 삭제해도될듯.
public record FeedLikeDto(
	FeedDto feed,
	AuthorDto author
) {
}
