package com.part4.team05.sb01otbooteam05.domain.notification.service;

import com.part4.team05.sb01otbooteam05.domain.notification.dto.NotificationDtoCursorResponse;

import java.util.UUID;

public interface NotificationService {
    NotificationDtoCursorResponse getNotifications(UUID userId, UUID idAfter, int limit);
}
