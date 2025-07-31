package com.part4.team05.sb01otbooteam05.domain.notification.service.impl;

import com.part4.team05.sb01otbooteam05.domain.notification.dto.NotificationDto;
import com.part4.team05.sb01otbooteam05.domain.notification.dto.NotificationDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.notification.entity.Notification;
import com.part4.team05.sb01otbooteam05.domain.notification.entity.NotificationLevel;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private static final Long TIMEOUT = 60L * 1000 * 60; // 1시간
    private final Map<UUID, List<SseEmitter>> emittersMap = new ConcurrentHashMap<>();

    @Override
    public NotificationDtoCursorResponse getNotifications(User user, String cursor, UUID idAfter, int limit) {
        UUID userId = user.getId();
        Pageable pageable = PageRequest.of(0, limit + 1);

        if (cursor != null) {
            try {
                byte[] decoded = Base64.getUrlDecoder().decode(cursor);
                String decodedStr = new String(decoded, StandardCharsets.UTF_8);
                idAfter = UUID.fromString(decodedStr);
            } catch (IllegalArgumentException ex) {
                // Base64 디코딩 또는 UUID 파싱 실패 시
                throw new OtbooException(ErrorCode.INVALID_CURSOR);
            }
        }

        List<Notification> results = notificationRepository.findNotifications(userId, idAfter, pageable);

        if (results.isEmpty() && idAfter == null) {
            log.info("userId={}에 대한 알림이 존재하지 않습니다.", userId);
            return new NotificationDtoCursorResponse(
                    Collections.emptyList(),
                    null,
                    null,
                    false,
                    0L,
                    "createdAt",
                    "DESC"
            );
        }

        boolean hasNext = results.size() > limit;
        if(hasNext) {
            results = results.subList(0, limit);
        }

        List<NotificationDto> dtos = results.stream()
                .map(NotificationMapper::toDto)
                .toList();

        UUID lastId = hasNext ? results.get(results.size() - 1).getId() : null;
        String nextCursor = hasNext
                ? Base64.getUrlEncoder().encodeToString(lastId.toString().getBytes(StandardCharsets.UTF_8))
                : null;

        long totalCount = notificationRepository.countByReceiverId(userId);

        return new NotificationDtoCursorResponse(
                dtos,
                nextCursor,
                lastId,
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

        if(!notification.getReceiverId().equals(userId)) {
            throw new OtbooException(ErrorCode.NOTIFICATION_UNAUTHORIZED);
        }

        notification.markAsRead();
        log.info("알림 읽음 처리 완료: notificationId={}", notificationId);
    }

    @Override
    public SseEmitter connect(UUID userId, UUID lastEventId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);

        emittersMap.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError((e) -> removeEmitter(userId, emitter));

        return emitter;
    }

    @Override
    public void replayMissed(UUID userId, UUID lastEventId, SseEmitter emitter) {
        // 배치 사이즈: 한 번에 조회할 최대 개수
        int batchSize = 100;
        // createdAt DESC, id DESC 순서로 정렬해서 페이지 요청 생성
        Pageable pageable = PageRequest.of(
                0, batchSize,
                Sort.by(
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id")
                )
        );

        List<Notification> missed;
        do {
            // 마지막으로 받은 이벤트 이후의 알림을 배치 단위로 조회
            missed = notificationRepository.findNotifications(userId, lastEventId, pageable);

            // 조회된 알림을 차례로 전송
            for (Notification n : missed) {
                try {
                    emitter.send(
                            SseEmitter.event()
                                    .id(n.getId().toString())
                                    .name("notifications")
                                    .data(NotificationMapper.toDto(n))
                    );
                } catch (IOException e) {
                    log.warn("missed notification 전송 실패: id={}, error={}", n.getId(), e.getMessage());
                    emitter.completeWithError(e);
                    return;
                }
            }

            // 다음 페이지로 이동
            pageable = pageable.next();
        } while (missed.size() == batchSize);
    }


    @Override
    public void sendNotification(NotificationDto notification) {
        // 1) DB 저장
        Notification entity = NotificationMapper.toEntity(notification);
        notificationRepository.save(entity);

        // 2) 실시간 전송 (멀티 클라이언트 지원)
        List<SseEmitter> emitters = emittersMap.get(notification.receiverId());
        if (emitters != null) {
            List<SseEmitter> dead = new ArrayList<>();
            for (SseEmitter em : emitters) {
                try {
                    em.send(
                            SseEmitter.event()
                                    .id(notification.id().toString())
                                    .name("notifications")
                                    .data(notification)
                    );
                } catch (IOException ex) {
                    dead.add(em);
                    em.completeWithError(ex);
                }
            }
            dead.forEach(e -> removeEmitter(notification.receiverId(), e));
        }
    }

    @Transactional
    public void createAndSendNotification(UUID receiverId, String title, String content, NotificationLevel level) {
        NotificationDto notificationDto = new NotificationDto(
                UUID.randomUUID(),
                LocalDateTime.now(),
                receiverId,
                title,
                content,
                level
        );

        sendNotification(notificationDto);
    }

    private void removeEmitter(UUID userId, SseEmitter emitter) {
        List<SseEmitter> list = emittersMap.get(userId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emittersMap.remove(userId);
            }
        }
    }
}
