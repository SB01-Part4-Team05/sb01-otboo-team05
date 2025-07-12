package com.part4.team05.sb01otbooteam05.domain.notification.dto;

import com.part4.team05.sb01otbooteam05.domain.notification.entity.NotificationLevel;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        LocalDateTime createdAt,
        UUID receiverId,
        String title,
        String content,
        NotificationLevel level
) {}