package com.part4.team05.sb01otbooteam05.domain.notification.mapper;

import com.part4.team05.sb01otbooteam05.domain.notification.dto.NotificationDto;
import com.part4.team05.sb01otbooteam05.domain.notification.entity.Notification;

public class NotificationMapper {

    public static NotificationDto toDto(Notification n) {
        return new NotificationDto(
                n.getId(),
                n.getCreatedAt(),
                n.getReceiverId(),
                n.getTitle(),
                n.getContent(),
                n.getLevel()
        );
    }

    public static Notification toEntity(NotificationDto dto) {
        return Notification.builder()
                .receiverId(dto.receiverId())
                .title(dto.title())
                .content(dto.content())
                .level(dto.level())
                .isRead(false)
                .build();
    }
}
