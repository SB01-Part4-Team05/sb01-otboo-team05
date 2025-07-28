package com.part4.team05.sb01otbooteam05.domain.follow.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FollowCreateRequest (
        @NotNull(message = "followeeId는 필수입니다") UUID followeeId,
        @NotNull(message = "followerId는 필수입니다") UUID followerId
) {}
