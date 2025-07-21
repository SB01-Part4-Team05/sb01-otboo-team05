package com.part4.team05.sb01otbooteam05.domain.notification.service;

import com.part4.team05.sb01otbooteam05.domain.notification.dto.NotificationDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.notification.entity.NotificationLevel;
import com.part4.team05.sb01otbooteam05.domain.notification.entity.NotificationType;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;

import java.util.UUID;

public interface NotificationService {
    NotificationDtoCursorResponse getNotifications(User user, UUID idAfter, int limit);

    void markAsRead(UUID notificationId, UUID userId);

    void sendNotification(UUID targetUserId, String title, String content, UUID feedId, NotificationType type, NotificationLevel level);
}
