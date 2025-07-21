package com.part4.team05.sb01otbooteam05.domain.notification.service.impl;

import com.part4.team05.sb01otbooteam05.domain.notification.dto.NotificationDto;
import com.part4.team05.sb01otbooteam05.domain.notification.dto.NotificationDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.notification.entity.Notification;
import com.part4.team05.sb01otbooteam05.domain.notification.entity.NotificationLevel;
import com.part4.team05.sb01otbooteam05.domain.notification.entity.NotificationType;
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
        if (hasNext) {
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
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new OtbooException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getReceiverId().equals(userId)) {
            throw new OtbooException(ErrorCode.NOTIFICATION_UNAUTHORIZED);
        }

        notification.markAsRead();
        log.info("알림 읽음 처리 완료: notificationId={}", notificationId);
    }

    // 피드에 댓글이 달릴 시 작성자에게 발송될 알림 생성 (댓글이 달린 피드의 작성자에게 알림을 보내라는 이벤트로 인해 동작함)
    // todo 메서드 만들었다고 알려드리기
    @Transactional
    @Override
    public void sendNotification(UUID targetUserId, String title, String content, UUID feedId, NotificationType type, NotificationLevel level) {
        Notification notification = Notification.builder()
                .receiverId(targetUserId)
                .title(title)
                .content(content)
                .type(type)
                .entityId(feedId)
                .isRead(false)
                .level(level)// 피드 ID 연관 알림 //todo 엔티티아이디에 피드아이디 들어가도되는건지 확인
                .build();

        notificationRepository.save(notification);
        log.info("댓글 알림 발송 완료 : notificationId={}, receiverId={}", notification.getId(), notification.getReceiverId());
    }
}
