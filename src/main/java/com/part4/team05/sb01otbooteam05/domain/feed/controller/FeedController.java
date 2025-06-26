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
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FeedCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FeedUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDto;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentsPageResponse;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.CommentCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.FindCommentsRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FindFeedsRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.service.FeedService;

import lombok.RequiredArgsConstructor;

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
	public ResponseEntity<FeedDto> createFeed(FeedCreateRequest request
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
	public ResponseEntity<CommentsPageResponse> findFeedComments(FindCommentsRequest request) {
		CommentsPageResponse feedCommentDtos = feedService.findComments(request);
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(feedCommentDtos);
	}

	@PostMapping("/{feedId}/comments")
	public ResponseEntity<CommentDto> createFeedComment(
		@PathVariable("feedId") UUID feedId,
		// @AuthenticationPrincipal CustomUserDetails user
		CommentCreateRequest request
	) {
		CommentDto commentDto = feedService.createFeedComment(feedId, request);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(commentDto);
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
		FeedUpdateRequest request
	){
		FeedDto feedDto = feedService.updateFeed(feedId, request);
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(feedDto);
	}
}
