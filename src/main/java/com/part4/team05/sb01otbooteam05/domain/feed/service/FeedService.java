package com.part4.team05.sb01otbooteam05.domain.feed.service;

import java.util.UUID;

import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDto;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedsPageResponse;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FeedCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FeedUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDto;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentsPageResponse;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.CommentCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.FindCommentsRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FindFeedsRequest;

public interface FeedService {

	FeedsPageResponse findFeeds(FindFeedsRequest request);

	FeedDto createFeed(FeedCreateRequest request);

	FeedDto likeFeed(UUID feedId);

	FeedDto unlikeFeed(UUID feedId);

	CommentDto createFeedComment(UUID feedId, CommentCreateRequest request);

	FeedDto deleteFeed(UUID feedId);

	FeedDto updateFeed(UUID feedId, FeedUpdateRequest request);

	CommentsPageResponse findComments(FindCommentsRequest request);
}
