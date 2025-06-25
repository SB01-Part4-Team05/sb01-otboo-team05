package com.part4.team05.sb01otbooteam05.domain.feed.service;

import java.util.UUID;

import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDto;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedsPageResponse;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.CreateFeedRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.UpdateFeedRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.FeedCommentDto;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.FeedCommentsPageResponse;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.CreateFeedCommentRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.FindFeedCommentsRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FindFeedsRequest;

public interface FeedService {

	FeedsPageResponse findFeeds(FindFeedsRequest request);

	FeedDto createFeed(CreateFeedRequest request);

	FeedDto likeFeed(UUID feedId);

	FeedDto unlikeFeed(UUID feedId);

	FeedCommentsPageResponse findFeedComments(FindFeedCommentsRequest request);

	FeedCommentDto createFeedComment(UUID feedId, CreateFeedCommentRequest request);

	FeedDto deleteFeed(UUID feedId);

	FeedDto updateFeed(UUID feedId, UpdateFeedRequest request);
}
