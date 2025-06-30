package com.part4.team05.sb01otbooteam05.domain.follow.dto;

import java.util.UUID;

public record FollowSummaryDto(
        UUID followeeId,
        long followerCount,
        long followingCount,
        boolean followedByMe,
        UUID followedByMeId,
        boolean followingMe
) {}

