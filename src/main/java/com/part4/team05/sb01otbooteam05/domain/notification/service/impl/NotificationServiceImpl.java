package com.part4.team05.sb01otbooteam05.domain.notification.service.impl;

import com.part4.team05.sb01otbooteam05.domain.notification.dto.NotificationDto;
import com.part4.team05.sb01otbooteam05.domain.notification.dto.NotificationDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.notification.entity.Notification;
import com.part4.team05.sb01otbooteam05.domain.notification.exception.NotificationNotFoundException;
import com.part4.team05.sb01otbooteam05.domain.notification.mapper.NotificationMapper;
import com.part4.team05.sb01otbooteam05.domain.notification.repository.NotificationRepository;
import com.part4.team05.sb01otbooteam05.domain.notification.service.NotificationService;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.exception.ErrorCode;
import com.part4.team05.sb01otbooteam05.exception.OtbooException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public NotificationDtoCursorResponse getNotifications(User user, UUID idAfter, int limit) {
        UUID userId = user.getId();
        Pageable pageable = PageRequest.of(0, limit + 1);

        List<Notification> results = notificationRepository.findNotifications(userId, idAfter, pageable);

        if ((results.isEmpty() && idAfter == null)) {
            log.warn("userId={}에 대한 알림이 존재하지 않음", userId);
            throw new NotificationNotFoundException();
        }

        boolean hasNext = results.size() > limit;
        if(hasNext) {
            results = results.subList(0, limit);
        }

        List<NotificationDto> dtos = results.stream()
                .map(NotificationMapper::toDto)
                .toList();

        UUID nextIdAfter = hasNext ? results.get(results.size() - 1).getId() : null;
        long totalCount = notificationRepository.countByReceiverId(userId);

        return new NotificationDtoCursorResponse(
                dtos,
                null,
                nextIdAfter,
                hasNext,
                totalCount,
                "createdAt",
                "DESCENDING"
        );
    }

    @Transactional
    @Override
    public void markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new OtbooException(ErrorCode.NOTIFICATION_NOT_FOUND));

        notification.markAsRead();
        log.info("알림 읽음 처리 완료: notificationId={}", notificationId);
    }
}
