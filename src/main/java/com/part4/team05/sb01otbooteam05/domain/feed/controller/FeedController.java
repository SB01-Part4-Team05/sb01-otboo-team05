package com.part4.team05.sb01otbooteam05.domain.feed.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDto;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedsPageResponse;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.CreateFeedRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.UpdateFeedRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.FeedCommentDto;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.FeedCommentsPageResponse;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.CreateFeedCommentRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.FindFeedCommentsRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FindFeedsRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.service.FeedService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feeds")
public class FeedController {

	//todo 유저 id 파라미터 넣기
	private final FeedService feedService;

	@GetMapping("/")
	public ResponseEntity<FeedsPageResponse> findFeeds(FindFeedsRequest request) {
		FeedsPageResponse feedDtos = feedService.findFeeds(request);
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(feedDtos);
	}

	@PostMapping("/")
	public ResponseEntity<FeedDto> createFeed(CreateFeedRequest request
		// @AuthenticationPrincipal CustomUserDetails user
	) {
		FeedDto feedDto = feedService.createFeed(request);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(feedDto);
	}

	@PostMapping("/{feedId}/like")
	public ResponseEntity<FeedDto> likeFeed(
		@PathVariable("feedId") UUID feedId
		// @AuthenticationPrincipal CustomUserDetails user
	) {
		FeedDto feedDto = feedService.likeFeed(feedId);
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(feedDto);
	}

	@DeleteMapping("/{feedId}/like")
	public ResponseEntity<FeedDto> unlikeFeed(
		@PathVariable("feedId") UUID feedId
		// @AuthenticationPrincipal CustomUserDetails user
	) {
		FeedDto feedDto = feedService.unlikeFeed(feedId);
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(feedDto);
	}

	@GetMapping("/{feedId}/comments")
	public ResponseEntity<FeedCommentsPageResponse> findFeedComments(FindFeedCommentsRequest request) {
		FeedCommentsPageResponse feedCommentDtos = feedService.findFeedComments(request);
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(feedCommentDtos);
	}

	@PostMapping("/{feedId}/comments")
	public ResponseEntity<FeedCommentDto> createFeedComment(
		@PathVariable("feedId") UUID feedId,
		// @AuthenticationPrincipal CustomUserDetails user
		CreateFeedCommentRequest request
	) {
		FeedCommentDto feedCommentDto = feedService.createFeedComment(feedId, request);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(feedCommentDto);
	}

	//todo 딜리트시 반환값 이거맞는지 확인 필요
	@DeleteMapping("/{feedId}")
	public ResponseEntity<FeedDto> deleteFeed(
		@PathVariable("feedId") UUID feedId
		// @AuthenticationPrincipal CustomUserDetails user
	){
		FeedDto feedDto = feedService.deleteFeed(feedId);
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(feedDto);
	}

	@PatchMapping("/{feedId}")
	public ResponseEntity<FeedDto> updateFeed(
		@PathVariable("feedId") UUID feedId,
		// @AuthenticationPrincipal CustomUserDetails user
		UpdateFeedRequest request
	){
		FeedDto feedDto = feedService.updateFeed(feedId, request);
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(feedDto);
	}
}
