package com.part4.team05.sb01otbooteam05.domain.follow.service;

import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowDto;

public interface FollowService {
    FollowDto createFollow(FollowCreateRequest request);
}
