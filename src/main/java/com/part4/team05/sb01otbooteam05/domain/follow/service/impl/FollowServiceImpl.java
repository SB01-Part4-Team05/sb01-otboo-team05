package com.part4.team05.sb01otbooteam05.domain.follow.service.impl;

import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowDto;
import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowListResponse;
import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowSummaryDto;
import com.part4.team05.sb01otbooteam05.domain.follow.entity.Follow;
import com.part4.team05.sb01otbooteam05.domain.follow.exception.FollowException;
import com.part4.team05.sb01otbooteam05.domain.follow.mapper.FollowMapper;
import com.part4.team05.sb01otbooteam05.domain.follow.repository.FollowRepository;
import com.part4.team05.sb01otbooteam05.domain.follow.service.FollowService;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;
import com.part4.team05.sb01otbooteam05.exception.ErrorCode;
import com.part4.team05.sb01otbooteam05.exception.OtbooException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.part4.team05.sb01otbooteam05.exception.ErrorCode.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final FollowMapper followMapper;

    @Transactional
    @Override
    public FollowDto createFollow(FollowCreateRequest request) {
        UUID followerId = request.followerId();
        UUID followeeId = request.followeeId();

        log.info("팔로우 생성 요청: follower={}, followee={}", followerId, followeeId);

        // user 존재 여부 확인
        if(!userRepository.existsById(followerId)) {
            log.warn("팔로워 ID가 존재하지 않음: {}", followerId);
            throw new FollowException(ErrorCode.USER_NOT_FOUND);
        }

        if(!userRepository.existsById(followeeId)) {
            log.warn("팔로이 ID가 존재하지 않음: {}", followeeId);
            throw new FollowException(ErrorCode.USER_NOT_FOUND);
        }

        // 자기 자신 팔로우 금지
        if(followerId.equals(followeeId)) {
            log.warn("자기 자신을 팔로우할 수 없음: {}", followerId);
            throw new FollowException(FOLLOW_SELF_NOT_ALLOWED);
        }

        // 중복 팔로우 방지
        boolean exists = followRepository.existsByFollowerAndFollowee(followerId, followeeId);
        if(exists) {
            log.warn("이미 팔로우된 관계: follower={}, followee={}", followerId, followeeId);
            throw new FollowException(ALREADY_FOLLOWED);
        }

        // 저장
        Follow follow = followMapper.toEntity(request);
        Follow saved = followRepository.save(follow);

        log.info("팔로우 저장 완료: followId={}, follower={}, followee={}", saved.getId(), followerId, followeeId);

        Map<UUID, User> userMap = userRepository.findAllById(List.of(followerId, followeeId)).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return followMapper.toDto(saved, userMap);
    }

    @Override
    public FollowSummaryDto getFollowSummary(UUID userId, UUID currentUserId) {
        long followerCount = followRepository.countByFollowee(userId);
        long followingCount = followRepository.countByFollower(userId);


        // 내가 그 유저를 팔로우하고 있는가?
        boolean followedByMe = followRepository.existsByFollowerAndFollowee(currentUserId, userId);

        // 그 유저가 나를 팔로우하고 있는가?
        boolean followingMe = followRepository.existsByFollowerAndFollowee(userId, currentUserId);

        return new FollowSummaryDto(
                userId,
                followerCount,
                followingCount,
                followedByMe,
                followedByMe ? currentUserId : null,
                followingMe
        );
    }

    // 팔로잉 목록 조회
    @Override
    public FollowListResponse getFollowings(UUID followerId, String cursor, UUID idAfter, int limit, String nameLike) {
        return getFollowList(
                followerId,
                "팔로잉",
                cursor,
                idAfter,
                limit,
                nameLike,
                (id, pageable) -> followRepository.findFollowings(id, idAfter, nameLike, pageable)
        );
    }

    // 팔로워 목록 조회
    @Override
    public FollowListResponse getFollowers(UUID followeeId, String cursor, UUID idAfter, int limit, String nameLike) {
        return getFollowList(
                followeeId,
                "팔로워",
                cursor,
                idAfter,
                limit,
                nameLike,
                (id, pageable) -> followRepository.findFollowers(id, idAfter, nameLike, pageable)
        );
    }

    private FollowListResponse getFollowList(
            UUID userId,
            String userType,
            String cursor,
            UUID idAfter,
            int limit,
            String nameLike,
            BiFunction<UUID, Pageable, List<Follow>> repositoryMethod
    ) {
        if (userId == null || !userRepository.existsById(userId)) {
            log.warn("{} 목록 조회 실패 - 존재하지 않는 사용자: userId={}", userType, userId);
            throw new OtbooException(ErrorCode.USER_NOT_FOUND);
        }

        log.debug("{} 목록 조회 시작: userId={}, idAfter={}, limit={}, nameLike={}", userType, userId, idAfter, limit, nameLike);

        Pageable pageable = PageRequest.of(0, limit + 1);
        List<Follow> follows = repositoryMethod.apply(userId, pageable);

        boolean hasNext = follows.size() > limit;
        if (hasNext) follows = follows.subList(0, limit);

        List<UUID> userIds = follows.stream()
                .flatMap(f -> Stream.of(f.getFollower(), f.getFollowee()))
                .distinct()
                .toList();

        Map<UUID, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<FollowDto> dtos = follows.stream()
                .map(f -> followMapper.toDto(f, userMap))
                .toList();

        UUID nextIdAfter = hasNext ? follows.get(follows.size() - 1).getId() : null;

        long totalCount = "팔로잉".equals(userType)
                ? followRepository.countByFollower(userId)
                : followRepository.countByFollowee(userId);

        String nextCursor = nextIdAfter != null ? nextIdAfter.toString() : "";

        log.info("{} 조회 완료: count={}, hasNext={}", userType, dtos.size(), hasNext);

        return new FollowListResponse(
                dtos,
                nextCursor,
                nextIdAfter,
                hasNext,
                totalCount,
                "id",
                "ASCENDING"
        );
    }

    @Override
    public void unfollow(UUID followId) {
        Follow follow = followRepository.findById(followId)
                .orElseThrow(() -> new FollowException(INVALID_REQUEST));

        followRepository.delete(follow);
        log.info("팔로우 취소 완료: followId={}", followId);
    }
}
