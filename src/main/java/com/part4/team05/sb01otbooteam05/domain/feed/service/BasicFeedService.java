package com.part4.team05.sb01otbooteam05.domain.feed.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDto;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedsPageResponse;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FeedCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FindFeedsRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FeedUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.repository.FeedRepository;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDto;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentsPageResponse;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.CommentCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.FindCommentsRequest;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BasicFeedService implements FeedService {

	private final FeedRepository feedRepository;

	@Override
	public FeedsPageResponse findFeeds(UUID userId, FindFeedsRequest request) {
		return null;
	}

	@Override
	public FeedDto createFeed(UUID userId, FeedCreateRequest request) {
		return null;
	}

	@Override
	public FeedDto likeFeed(UUID userId, UUID feedId) {
		return null;
	}

	@Override
	public FeedDto unlikeFeed(UUID userId, UUID feedId) {
		return null;
	}

	@Override
	public CommentDto createFeedComment(UUID userId, UUID feedId, CommentCreateRequest request) {
		return null;
	}

	@Override
	public FeedDto deleteFeed(UUID userId, UUID feedId) {
		return null;
	}

	@Override
	public FeedDto updateFeed(UUID userId, UUID feedId, FeedUpdateRequest request) {
		return null;
	}

	@Override
	public CommentsPageResponse findComments(UUID userId, FindCommentsRequest request) {
		return null;
	}
}
