package com.part4.team05.sb01otbooteam05.domain.feed.service;

import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDto;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FeedCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FeedUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FindFeedsRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDto;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentsPageResponse;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.CommentCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.FindCommentsRequest;

import java.util.UUID;

public interface FeedService {

	FeedDtoCursorResponse findFeeds(UUID userId, FindFeedsRequest request);

	FeedDto createFeed(UUID userId, FeedCreateRequest request);

	void deleteFeed(UUID userId, UUID feedId);

	FeedDto likeFeed(UUID userId, UUID feedId);

	FeedDto unlikeFeed(UUID userId, UUID feedId);

	CommentDto createFeedComment(UUID userId, UUID feedId, CommentCreateRequest request);


	FeedDto updateFeed(UUID userId, UUID feedId, FeedUpdateRequest request);

	CommentsPageResponse findComments(UUID userId, FindCommentsRequest request);
}
