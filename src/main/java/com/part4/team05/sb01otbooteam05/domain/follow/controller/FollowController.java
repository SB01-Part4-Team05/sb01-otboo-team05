package com.part4.team05.sb01otbooteam05.domain.follow.controller;

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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/follows")
public class FollowController {

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
            @RequestParam UUID currentUserId // 추후 로그인 사용자에서 추출하도록 수정 예정
    ) {
        FollowSummaryDto result = followService.getFollowSummary(userId, currentUserId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/followings")
    public FollowListResponse getFollowings(
            @RequestParam @NotNull UUID followerId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) UUID idAfter,
            @RequestParam @Min(1) int limit,
            @RequestParam(required = false) String nameLike
    ) {
        log.info("팔로잉 목록 조회 요청: followerId={}, limit={}, idAfter={}, nameLike={}", followerId, limit, idAfter, nameLike);
        return followService.getFollowings(followerId, cursor, idAfter, limit, nameLike);
    }

    @GetMapping("/followers")
    public FollowListResponse getFollowers(
            @RequestParam @NotNull UUID followeeId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) UUID idAfter,
            @RequestParam @Min(1) int limit,
            @RequestParam(required = false) String nameLike
    ) {
        log.info("팔로워 목록 조회 요청: followeeId={}, limit={}, idAfter={}, nameLike={}", followeeId, limit, idAfter, nameLike);
        return followService.getFollowers(followeeId, cursor, idAfter, limit, nameLike);
    }
}
