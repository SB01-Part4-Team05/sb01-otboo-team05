package com.part4.team05.sb01otbooteam05.domain.follow.dto;

import com.part4.team05.sb01otbooteam05.domain.user.dto.UserSummary;

import java.util.UUID;

public record FollowDto (
        UUID id,
        UserSummary followee,
        UserSummary follower
) {
}
