package com.part4.team05.sb01otbooteam05.domain.directMessage.dto;

import java.time.Instant;
import java.util.UUID;

public record DirectMessageDto(
        UUID id,
        Instant createdAt,
//        UserSummary sender,
//        UserSummary receiver,
        String content
) {}

