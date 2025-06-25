package com.part4.team05.sb01otbooteam05.domain.feed.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDto;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedsPageResponse;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.CreateFeedRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FindFeedsRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.UpdateFeedRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.repository.FeedRepository;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.FeedCommentDto;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.FeedCommentsPageResponse;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.CreateFeedCommentRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.FindFeedCommentsRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BasicFeedService implements FeedService {

	private final FeedRepository feedRepository;

	@Override
	public FeedsPageResponse findFeeds(FindFeedsRequest request) {
		return null;
	}

	@Override
	public FeedDto createFeed(CreateFeedRequest request) {
		return null;
	}

	@Override
	public FeedDto likeFeed(UUID feedId) {
		return null;
	}

	@Override
	public FeedDto unlikeFeed(UUID feedId) {
		return null;
	}

	@Override
	public FeedCommentsPageResponse findFeedComments(FindFeedCommentsRequest request) {
		return null;
	}

	@Override
	public FeedCommentDto createFeedComment(UUID feedId, CreateFeedCommentRequest request) {
		return null;
	}

	@Override
	public FeedDto deleteFeed(UUID feedId) {
		return null;
	}

	@Override
	public FeedDto updateFeed(UUID feedId, UpdateFeedRequest request) {
		return null;
	}
}
