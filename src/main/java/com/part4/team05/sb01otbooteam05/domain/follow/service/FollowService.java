package com.part4.team05.sb01otbooteam05.domain.follow.service;

import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowDto;
import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowSummaryDto;

import java.util.UUID;

public interface FollowService {
    FollowDto createFollow(FollowCreateRequest request);

    FollowSummaryDto getFollowSummary(UUID userId, UUID currentUserId);
}
