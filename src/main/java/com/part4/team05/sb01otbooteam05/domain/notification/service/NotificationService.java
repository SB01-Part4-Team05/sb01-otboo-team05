package com.part4.team05.sb01otbooteam05.domain.notification.service;

import com.part4.team05.sb01otbooteam05.domain.notification.dto.NotificationDto;
import com.part4.team05.sb01otbooteam05.domain.notification.dto.NotificationDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

public interface NotificationService {
    NotificationDtoCursorResponse getNotifications(User user, UUID idAfter, int limit);

    void markAsRead(UUID notificationId, UUID userId);

    SseEmitter connect(UUID userId, UUID lastEventId);

    void sendNotification(NotificationDto notification);
}
