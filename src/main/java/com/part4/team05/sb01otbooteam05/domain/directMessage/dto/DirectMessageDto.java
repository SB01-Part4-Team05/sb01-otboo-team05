package com.part4.team05.sb01otbooteam05.domain.directMessage.dto;

import com.part4.team05.sb01otbooteam05.domain.user.dto.UserSummary;

import java.time.LocalDateTime;
import java.util.UUID;

public record DirectMessageDto(
        UUID id,
        LocalDateTime createdAt,
        UserSummary sender,
        UserSummary receiver,
        String content
) {}

