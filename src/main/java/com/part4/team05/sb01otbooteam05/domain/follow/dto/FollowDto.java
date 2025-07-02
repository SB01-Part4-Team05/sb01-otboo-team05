package com.part4.team05.sb01otbooteam05.domain.follow.dto;

import java.util.UUID;

public record FollowDto (
    UUID id,
    UserSummary followee,
    UserSummary follower
) {
    public record UserSummary (
            UUID userId,
            String name, // 추후 User 연동 시 채움
            String profileImageUrl
    ) {}
}
