package com.part4.team05.sb01otbooteam05.domain.feed.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.query.SortDirection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDto;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FeedCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FeedUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FindFeedsRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.enums.SortType;
import com.part4.team05.sb01otbooteam05.domain.feed.service.FeedService;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDto;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.CommentCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.FindCommentsRequest;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.PrecipitationType;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.SkyStatusType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feeds")
public class FeedController {

    private final FeedService feedService;


    // 피드 목록조회
    @GetMapping
    public ResponseEntity<FeedDtoCursorResponse> findFeeds(
            @AuthenticationPrincipal CustomUserDetails user,
            // 값이 들어왔는데 변환할 타입과 맞지않을경우(ex. 지정한 상수로 변환될 수 없는 문자 들어옴) 스프링이 400 반환)
            @RequestParam(value = "cursor", defaultValue = "", required = false) String cursor,
            @RequestParam(value = "idAfter", required = false) UUID idAfter,
            @RequestParam(value = "limit", defaultValue = "20", required = true) Integer limit,
            @RequestParam(value = "sortBy", required = false) SortType sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "ASCENDING", required = true) SortDirection sortDirection,
            @RequestParam(value = "keywordLike", defaultValue = "", required = false) String keywordLike,
            @RequestParam(value = "skyStatusEqual", required = false) SkyStatusType skyStatusEqual,
            @RequestParam(value = "precipitationTypeEqual", required = false) PrecipitationType precipitationTypeEqual,
            @RequestParam(value = "authorIdEqual", required = false) UUID authorIdEqual
    ) {
        UUID userId = user.getUserId();
        FindFeedsRequest request = new FindFeedsRequest(cursor, idAfter, limit, sortBy, sortDirection, keywordLike, skyStatusEqual, precipitationTypeEqual, authorIdEqual);
        FeedDtoCursorResponse foundFeedsPageDto = feedService.findFeeds(userId, request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(foundFeedsPageDto);
    }

    //피드 생성
    @PostMapping
    public ResponseEntity<FeedDto> createFeed(
            @AuthenticationPrincipal CustomUserDetails user,
            @Validated @RequestBody FeedCreateRequest request
    ) {
        UUID userId = user.getUserId();
        FeedDto feedDto = feedService.createFeed(userId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(feedDto);
    }

    @PostMapping("{feedId}/like")
    public ResponseEntity<FeedDto> likeFeed(
            @PathVariable("feedId") UUID feedId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        UUID userId = user.getUserId();
        FeedDto feedDto = feedService.likeFeed(userId, feedId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(feedDto);
    }

    @DeleteMapping("{feedId}/like")
    public ResponseEntity<FeedDto> unlikeFeed(
            @PathVariable("feedId") UUID feedId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        UUID userId = user.getUserId();
        FeedDto feedDto = feedService.unlikeFeed(userId, feedId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(feedDto);
    }

    @GetMapping("{feedId}/comments")
    public ResponseEntity<CommentDtoCursorResponse> findFeedComments(
            @AuthenticationPrincipal CustomUserDetails user,
            // 값이 들어왔는데 변환할 타입과 맞지않을경우(ex. 지정한 상수로 변환될 수 없는 문자 들어옴) 스프링이 400 반환)
            @RequestParam(value = "feedId", required = true) UUID feedId,
            @RequestParam(value = "cursor", defaultValue = "")LocalDateTime cursor,
            @RequestParam(value = "idAfter", defaultValue = "") UUID idAfter,
            @RequestParam(value = "limit") Integer limit
    ) {
        UUID userId = user.getUserId();
        FindCommentsRequest request = new FindCommentsRequest(feedId, cursor, idAfter, limit);
        CommentDtoCursorResponse feedCommentDtos = feedService.findComments(userId, request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(feedCommentDtos);
    }

    @PostMapping("{feedId}/comments")
    public ResponseEntity<CommentDto> createFeedComment(
            @PathVariable("feedId") UUID feedId,
            @AuthenticationPrincipal CustomUserDetails user,
            @Validated @RequestBody CommentCreateRequest request
    ) {
        UUID userId = user.getUserId();
        CommentDto commentDto = feedService.createFeedComment(userId, feedId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(commentDto);
    }

    @DeleteMapping("{feedId}")
    public ResponseEntity<Void> deleteFeed(
            @PathVariable("feedId") UUID feedId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        UUID userId = user.getUserId();
        feedService.deleteFeed(userId, feedId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PatchMapping("{feedId}")
    public ResponseEntity<FeedDto> updateFeed(
            @PathVariable("feedId") UUID feedId,
            @AuthenticationPrincipal CustomUserDetails user,
            @Validated @RequestBody FeedUpdateRequest request
    ) {
        UUID userId = user.getUserId();
        FeedDto feedDto = feedService.updateFeed(userId, feedId, request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(feedDto);
    }
}
