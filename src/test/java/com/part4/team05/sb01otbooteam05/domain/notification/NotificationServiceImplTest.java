package com.part4.team05.sb01otbooteam05.domain.notification;

import com.part4.team05.sb01otbooteam05.domain.notification.dto.NotificationDto;
import com.part4.team05.sb01otbooteam05.domain.notification.dto.NotificationDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.notification.entity.Notification;
import com.part4.team05.sb01otbooteam05.domain.notification.entity.NotificationLevel;
import com.part4.team05.sb01otbooteam05.domain.notification.mapper.NotificationMapper;
import com.part4.team05.sb01otbooteam05.domain.notification.repository.NotificationRepository;
import com.part4.team05.sb01otbooteam05.domain.notification.service.impl.NotificationServiceImpl;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.exception.ErrorCode;
import com.part4.team05.sb01otbooteam05.exception.OtbooException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl(notificationRepository);
    }

    @Test
    void testGetNotificationsThrowsWhenEmptyAndIdAfterNull() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(notificationRepository.findNotifications(eq(userId), isNull(), any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        OtbooException ex = assertThrows(OtbooException.class,
                () -> service.getNotifications(user, /*cursor=*/null, /*idAfter=*/null, /*limit=*/5)
        );
        assertEquals(ErrorCode.NOTIFICATION_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void testGetNotificationsNoNext() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);

        NotificationDto dto = new NotificationDto(UUID.randomUUID(), LocalDateTime.now(), userId, "Title", "Content", NotificationLevel.INFO);
        Notification entity = NotificationMapper.toEntity(dto);
        when(notificationRepository.findNotifications(eq(userId), isNull(), any(Pageable.class)))
                .thenReturn(Collections.singletonList(entity));
        when(notificationRepository.countByReceiverId(userId)).thenReturn(1L);

        NotificationDtoCursorResponse response =
                service.getNotifications(user, /*cursor=*/null, /*idAfter=*/null, /*limit=*/5);

        assertNotNull(response);
        assertFalse(response.hasNext());
        assertEquals(1, response.data().size());
        assertNull(response.nextIdAfter());
        assertEquals(1L, response.totalCount());
    }

    @Test
    void testGetNotificationsWithNext() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);

        // 1) DTO 생성
        NotificationDto dto1 = new NotificationDto(
                UUID.randomUUID(),
                LocalDateTime.now().minusMinutes(1),
                userId, "T1", "C1", NotificationLevel.INFO
        );
        NotificationDto dto2 = new NotificationDto(
                UUID.randomUUID(),
                LocalDateTime.now(),
                userId, "T2", "C2", NotificationLevel.INFO
        );

        // 2) 엔티티 변환 후, 테스트용으로 id/createdAt 직접 주입
        Notification e1 = NotificationMapper.toEntity(dto1);
        Notification e2 = NotificationMapper.toEntity(dto2);
        setField(e1, "id", dto1.id());
        setField(e1, "createdAt", dto1.createdAt());
        setField(e2, "id", dto2.id());
        setField(e2, "createdAt", dto2.createdAt());

        // 3) 리포지토리 스텁 설정
        when(notificationRepository.findNotifications(eq(userId), isNull(), any(Pageable.class)))
                .thenReturn(Arrays.asList(e1, e2));
        when(notificationRepository.countByReceiverId(userId)).thenReturn(2L);

        // 4) 실행 및 검증
        NotificationDtoCursorResponse response =
                service.getNotifications(user, /*cursor=*/null, /*idAfter=*/null, /*limit=*/1);

        assertTrue(response.hasNext());
        assertEquals(e1.getId(),   response.nextIdAfter());
        assertEquals(2L,           response.totalCount());
    }


    @Test
    void testMarkAsReadSuccess() {
        UUID userId = UUID.randomUUID();
        UUID notifId = UUID.randomUUID();
        NotificationDto dto = new NotificationDto(notifId, LocalDateTime.now(), userId, "Title", "Content", NotificationLevel.INFO);
        Notification entity = NotificationMapper.toEntity(dto);
        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(entity));

        assertDoesNotThrow(() -> service.markAsRead(notifId, userId));
    }

    @Test
    void testMarkAsReadNotFound() {
        UUID notifId = UUID.randomUUID();
        when(notificationRepository.findById(notifId)).thenReturn(Optional.empty());
        OtbooException ex = assertThrows(OtbooException.class,
                () -> service.markAsRead(notifId, UUID.randomUUID()));
        assertEquals(ErrorCode.NOTIFICATION_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void testMarkAsReadUnauthorized() {
        UUID userId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        UUID notifId = UUID.randomUUID();
        NotificationDto dto = new NotificationDto(notifId, LocalDateTime.now(), otherId, "T", "C", NotificationLevel.INFO);
        Notification entity = NotificationMapper.toEntity(dto);
        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(entity));

        OtbooException ex = assertThrows(OtbooException.class,
                () -> service.markAsRead(notifId, userId));
        assertEquals(ErrorCode.NOTIFICATION_UNAUTHORIZED, ex.getErrorCode());
    }

    @Test
    void testConnectReturnsEmitter() {
        UUID userId = UUID.randomUUID();
        SseEmitter emitter = service.connect(userId, null);
        assertNotNull(emitter);
    }

    @Test
    void testSendNotificationWithoutEmitters() {
        NotificationDto notification = new NotificationDto(UUID.randomUUID(), LocalDateTime.now(), UUID.randomUUID(), "T", "C", NotificationLevel.INFO);
        when(notificationRepository.save(any())).thenReturn(null);

        assertDoesNotThrow(() -> service.sendNotification(notification));
        verify(notificationRepository).save(any());
    }

    @Test
    void testSendNotificationWithEmitters() throws Exception {
        UUID receiverId = UUID.randomUUID();
        NotificationDto notification = new NotificationDto(UUID.randomUUID(), LocalDateTime.now(), receiverId, "T", "C", NotificationLevel.INFO);
        when(notificationRepository.save(any())).thenReturn(null);

        Field field = NotificationServiceImpl.class.getDeclaredField("emittersMap");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<UUID, List<SseEmitter>> emittersMap = (Map<UUID, List<SseEmitter>>) field.get(service);
        SseEmitter emitter = mock(SseEmitter.class);
        emittersMap.put(receiverId, new CopyOnWriteArrayList<>(Collections.singletonList(emitter)));

        service.sendNotification(notification);
        verify(emitter).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    void testSendNotificationRemovesDeadEmitters() throws Exception {
        UUID receiverId = UUID.randomUUID();
        NotificationDto notification = new NotificationDto(UUID.randomUUID(), LocalDateTime.now(), receiverId, "T", "C", NotificationLevel.INFO);
        when(notificationRepository.save(any())).thenReturn(null);

        Field field = NotificationServiceImpl.class.getDeclaredField("emittersMap");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<UUID, List<SseEmitter>> emittersMap = (Map<UUID, List<SseEmitter>>) field.get(service);
        SseEmitter emitter = mock(SseEmitter.class);
        doThrow(new IOException("error")).when(emitter).send(any(SseEmitter.SseEventBuilder.class));
        emittersMap.put(receiverId, new CopyOnWriteArrayList<>(Collections.singletonList(emitter)));

        service.sendNotification(notification);
        verify(emitter).completeWithError(any(IOException.class));
        assertTrue(emittersMap.getOrDefault(receiverId, Collections.emptyList()).isEmpty());
    }

    @Test
    @DisplayName("SseEmitter가 complete된 후 send하면 예외 발생하고 제거되는지 확인")
    void testEmitterRemovalWhenSendAfterCompletion() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        // emitter1: 이미 완료된 emitter (IOException 발생 시 제거 대상)
        SseEmitter emitter1 = mock(SseEmitter.class);
        doThrow(new IOException("ResponseBodyEmitter has already completed"))
                .when(emitter1).send(any(SseEmitter.SseEventBuilder.class));
        doNothing().when(emitter1).completeWithError(any());

        // emitter2: 정상 동작
        SseEmitter emitter2 = mock(SseEmitter.class);

        // emittersMap 직접 삽입
        Field field = NotificationServiceImpl.class.getDeclaredField("emittersMap");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<UUID, List<SseEmitter>> emittersMap = (Map<UUID, List<SseEmitter>>) field.get(service);
        emittersMap.put(userId, new CopyOnWriteArrayList<>(List.of(emitter1, emitter2)));

        NotificationDto dto = new NotificationDto(
                UUID.randomUUID(), LocalDateTime.now(), userId,
                "제목", "내용", NotificationLevel.INFO
        );

        // when
        service.sendNotification(dto);

        // then
        List<SseEmitter> remaining = emittersMap.get(userId);
        assertThat(remaining).containsExactly(emitter2); // emitter1 제거 확인
        verify(emitter1).completeWithError(any(IOException.class)); // 예외 처리 확인
        verify(emitter2).send(any(SseEmitter.SseEventBuilder.class)); // 정상 작동 확인
    }


    @Test
    void testCreateAndSendNotification_invokesSendNotification() {
        NotificationServiceImpl spySvc = spy(new NotificationServiceImpl(notificationRepository));
        ArgumentCaptor<NotificationDto> captor = ArgumentCaptor.forClass(NotificationDto.class);
        doNothing().when(spySvc).sendNotification(any(NotificationDto.class));

        UUID rid = UUID.randomUUID();
        String title = "제목";
        String content = "본문";
        NotificationLevel level = NotificationLevel.WARNING;

        spySvc.createAndSendNotification(rid, title, content, level);

        verify(spySvc, times(1)).sendNotification(captor.capture());
        NotificationDto dto = captor.getValue();
        assertEquals(rid, dto.receiverId());
        assertEquals(title, dto.title());
        assertEquals(content, dto.content());
        assertEquals(level, dto.level());
        assertNotNull(dto.id());
        assertNotNull(dto.createdAt());
    }

    @Test
    void testReplayMissedBatchAndTermination() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID lastEventId = UUID.randomUUID();
        SseEmitter emitter = mock(SseEmitter.class);

        // 1) DTO 생성
        NotificationDto dto1 = new NotificationDto(
                UUID.randomUUID(),
                LocalDateTime.now(),
                userId, "T1", "C1", NotificationLevel.INFO
        );
        NotificationDto dto2 = new NotificationDto(
                UUID.randomUUID(),
                LocalDateTime.now(),
                userId, "T2", "C2", NotificationLevel.INFO
        );

        // 2) 엔티티 변환 후, id 주입
        Notification n1 = NotificationMapper.toEntity(dto1);
        Notification n2 = NotificationMapper.toEntity(dto2);
        // reflection 으로 private id 필드를 채운다
        setField(n1, "id", dto1.id());
        setField(n2, "id", dto2.id());

        // 3) 리포지토리 스텁: 첫 배치에 두 개, 두 번째는 빈 리스트
        when(notificationRepository.findNotifications(
                eq(userId), eq(lastEventId), any(Pageable.class))
        ).thenReturn(Arrays.asList(n1, n2))
                .thenReturn(Collections.emptyList());

        // 4) 실행
        service.replayMissed(userId, lastEventId, emitter);

        // send(SseEventBuilder) 가 정확히 두 번 호출됐는지 확인
         verify(emitter, times(2)).send(any(SseEmitter.SseEventBuilder.class));
         // 그 외 추가 호출이 없는지 확인
         verifyNoMoreInteractions(emitter);
    }

    @Test
    void testGetNotificationsInvalidCursorThrows() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);

        OtbooException ex = assertThrows(OtbooException.class,
                () -> service.getNotifications(user, "not-base64!!", null, 5)
        );
        assertEquals(ErrorCode.INVALID_CURSOR, ex.getErrorCode());
    }
}
