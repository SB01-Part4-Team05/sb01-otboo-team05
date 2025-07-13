package com.part4.team05.sb01otbooteam05.domain.follow.dto;

import java.util.UUID;

public record FollowDto (
    UUID id,
    UUID followee,
    UUID follower
) {
}
