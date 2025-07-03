package com.part4.team05.sb01otbooteam05.domain.follow.service.impl;

import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowDto;
import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowSummaryDto;
import com.part4.team05.sb01otbooteam05.domain.follow.entity.Follow;
import com.part4.team05.sb01otbooteam05.domain.follow.mapper.FollowMapper;
import com.part4.team05.sb01otbooteam05.domain.follow.repository.FollowRepository;
import com.part4.team05.sb01otbooteam05.domain.follow.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final FollowMapper followMapper;

    @Override
    public FollowDto createFollow(FollowCreateRequest request) {
        UUID followerId = request.followerId();
        UUID followeeId = request.followeeId();

        if(followerId.equals(followeeId)) {
            throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
        }

        boolean exists = followRepository.existsByFollowerAndFollowee(followerId, followeeId);
        if(exists) {
            throw new IllegalStateException("이미 팔로우한 사용자입니다.");
        }

        Follow follow = followMapper.toEntity(request);
        Follow saved = followRepository.save(follow);

        return followMapper.toDto(saved);
    }

    @Override
    public FollowSummaryDto getFollowSummary(UUID userId, UUID currentUserId) {
        long followerCount = followRepository.countByFollowee(userId);
        long followingCount = followRepository.countByFollower(userId);

        boolean followedByMe = followRepository.existsByFollowerAndFollowee(userId, currentUserId);
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
}
