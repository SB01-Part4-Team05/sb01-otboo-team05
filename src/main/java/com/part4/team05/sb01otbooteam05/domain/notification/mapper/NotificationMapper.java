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
}
