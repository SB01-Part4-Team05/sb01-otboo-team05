package com.part4.team05.sb01otbooteam05.domain.follow.controller;

import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowDto;
import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowListResponse;
import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowSummaryDto;
import com.part4.team05.sb01otbooteam05.domain.follow.service.FollowService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/follows")
public class FollowController implements FollowControllerDoc {

    private final FollowService followService;

    @PostMapping
    public ResponseEntity<FollowDto> createFollow(@RequestBody @Valid FollowCreateRequest request) {
        log.info("팔로우 생성 요청 수신 - follower={}, followee={}", request.followerId(), request.followeeId());

        FollowDto result = followService.createFollow(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/summary")
    public ResponseEntity<FollowSummaryDto> getFollowSummary(
            @RequestParam UUID userId,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        UUID currentUserId = me.getUserId();
        log.info("팔로우 요약 조회: targetUserId={}, currentUserId={}", userId, currentUserId);

        FollowSummaryDto summary = followService.getFollowSummary(userId, currentUserId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/followings")
    public ResponseEntity<FollowListResponse> getFollowings(
            @RequestParam @NotNull UUID followerId,
            @RequestParam(required = false) String cursor,
            @RequestParam @Min(1) int limit,
            @RequestParam(required = false) String nameLike
    ) {
        UUID idAfter = parseCursor(cursor);
        log.info("팔로잉 목록 조회 요청: followerId={}, cursor={}, idAfter={}, limit={}, nameLike={}",
                followerId, cursor, idAfter, limit, nameLike);
        FollowListResponse response =
                followService.getFollowings(followerId, idAfter, limit, nameLike);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/followers")
    public ResponseEntity<FollowListResponse> getFollowers(
            @RequestParam @NotNull UUID followeeId,
            @RequestParam(required = false) String cursor,
            @RequestParam @Min(1) int limit,
            @RequestParam(required = false) String nameLike
    ) {
        UUID idAfter = parseCursor(cursor);
        log.info("팔로워 목록 조회 요청: followeeId={}, cursor={}, idAfter={}, limit={}, nameLike={}",
                followeeId, cursor, idAfter, limit, nameLike);
        FollowListResponse response =
                followService.getFollowers(followeeId, idAfter, limit, nameLike);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{followId}")
    public ResponseEntity<Void> unfollow(@PathVariable UUID followId,
                                         @AuthenticationPrincipal CustomUserDetails me
    ) {
        UUID currentUserId = me.getUserId();
        log.info("팔로우 취소 요청: followId={}, currentUserId={}", followId, currentUserId);

        followService.unfollow(followId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    /** cursor(String) 를 UUID idAfter 로 변환 */
    private UUID parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) return null;
        try {
            return UUID.fromString(cursor);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 cursor: " + cursor);
        }
    }
}
