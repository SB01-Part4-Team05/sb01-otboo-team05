package com.part4.team05.sb01otbooteam05.domain.user.dto;

import java.util.UUID;

public record UserSummary(UUID userId,
                          String name,
                          String profileImageUrl) {
}
