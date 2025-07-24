package com.part4.team05.sb01otbooteam05.domain.notification.serivce.impl;

import com.part4.team05.sb01otbooteam05.domain.notification.dto.NotificationDto;
import com.part4.team05.sb01otbooteam05.domain.notification.dto.NotificationDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.notification.entity.NotificationLevel;
import com.part4.team05.sb01otbooteam05.domain.notification.exception.NotificationNotFoundException;
import com.part4.team05.sb01otbooteam05.domain.notification.service.impl.NotificationServiceImpl;
import com.part4.team05.sb01otbooteam05.exception.ErrorCode;
import com.part4.team05.sb01otbooteam05.exception.OtbooException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private com.part4.team05.sb01otbooteam05.domain.notification.repository.NotificationRepository notificationRepository;

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl(notificationRepository);
    }

    @Test
    void testGetNotificationsThrowsWhenEmptyAndIdAfterNull() {
        UUID userId = UUID.randomUUID();
        com.part4.team05.sb01otbooteam05.domain.user.entity.User user = mock(com.part4.team05.sb01otbooteam05.domain.user.entity.User.class);
        when(user.getId()).thenReturn(userId);
        when(notificationRepository.findNotifications(eq(userId), isNull(), any(Pageable.class))).thenReturn(Collections.emptyList());

        assertThrows(NotificationNotFoundException.class, () ->
                service.getNotifications(user, null, 5)
        );
    }

    @Test
    void testGetNotificationsNoNext() {
        UUID userId = UUID.randomUUID();
        com.part4.team05.sb01otbooteam05.domain.user.entity.User user = mock(com.part4.team05.sb01otbooteam05.domain.user.entity.User.class);
        when(user.getId()).thenReturn(userId);

        NotificationDto dto = new NotificationDto(UUID.randomUUID(), LocalDateTime.now(), userId, "Title", "Content", NotificationLevel.INFO);
        com.part4.team05.sb01otbooteam05.domain.notification.entity.Notification entity = com.part4.team05.sb01otbooteam05.domain.notification.mapper.NotificationMapper.toEntity(dto);
        when(notificationRepository.findNotifications(eq(userId), isNull(), any(Pageable.class)))
                .thenReturn(Collections.singletonList(entity));
        when(notificationRepository.countByReceiverId(userId)).thenReturn(1L);

        NotificationDtoCursorResponse response = service.getNotifications(user, null, 5);
        assertNotNull(response);
        assertFalse(response.hasNext());
        assertEquals(1, response.data().size());
        assertNull(response.nextIdAfter());
        assertEquals(1L, response.totalCount());
    }

    @Test
    void testGetNotificationsWithNext() {
        UUID userId = UUID.randomUUID();
        com.part4.team05.sb01otbooteam05.domain.user.entity.User user = mock(com.part4.team05.sb01otbooteam05.domain.user.entity.User.class);
        when(user.getId()).thenReturn(userId);

        NotificationDto dto1 = new NotificationDto(UUID.randomUUID(), LocalDateTime.now().minusMinutes(1), userId, "T1", "C1", NotificationLevel.INFO);
        NotificationDto dto2 = new NotificationDto(UUID.randomUUID(), LocalDateTime.now(), userId, "T2", "C2", NotificationLevel.INFO);
        com.part4.team05.sb01otbooteam05.domain.notification.entity.Notification e1 = com.part4.team05.sb01otbooteam05.domain.notification.mapper.NotificationMapper.toEntity(dto1);
        com.part4.team05.sb01otbooteam05.domain.notification.entity.Notification e2 = com.part4.team05.sb01otbooteam05.domain.notification.mapper.NotificationMapper.toEntity(dto2);
        when(notificationRepository.findNotifications(eq(userId), isNull(), any(Pageable.class)))
                .thenReturn(Arrays.asList(e1, e2));
        when(notificationRepository.countByReceiverId(userId)).thenReturn(2L);

        NotificationDtoCursorResponse response = service.getNotifications(user, null, 1);
        assertNotNull(response);
        assertTrue(response.hasNext());
        assertEquals(1, response.data().size());
        assertEquals(e1.getId(), response.nextIdAfter());
        assertEquals(2L, response.totalCount());
    }

    @Test
    void testMarkAsReadSuccess() {
        UUID userId = UUID.randomUUID();
        UUID notifId = UUID.randomUUID();
        NotificationDto dto = new NotificationDto(notifId, LocalDateTime.now(), userId, "Title", "Content", NotificationLevel.INFO);
        com.part4.team05.sb01otbooteam05.domain.notification.entity.Notification entity = com.part4.team05.sb01otbooteam05.domain.notification.mapper.NotificationMapper.toEntity(dto);
        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(entity));

        assertDoesNotThrow(() -> service.markAsRead(notifId, userId));
    }

    @Test
    void testMarkAsReadNotFound() {
        UUID userId = UUID.randomUUID();
        UUID notifId = UUID.randomUUID();
        when(notificationRepository.findById(notifId)).thenReturn(Optional.empty());

        OtbooException ex = assertThrows(OtbooException.class, () -> service.markAsRead(notifId, userId));
        assertEquals(ErrorCode.NOTIFICATION_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void testMarkAsReadUnauthorized() {
        UUID userId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        UUID notifId = UUID.randomUUID();
        NotificationDto dto = new NotificationDto(notifId, LocalDateTime.now(), otherId, "T", "C", NotificationLevel.INFO);
        com.part4.team05.sb01otbooteam05.domain.notification.entity.Notification entity = com.part4.team05.sb01otbooteam05.domain.notification.mapper.NotificationMapper.toEntity(dto);
        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(entity));

        OtbooException ex = assertThrows(OtbooException.class, () -> service.markAsRead(notifId, userId));
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

        // inject mock emitter
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
    void testCreateAndSendNotification() {
        NotificationServiceImpl spyService = spy(new NotificationServiceImpl(notificationRepository));
        doNothing().when(spyService).sendNotification(any());

        UUID receiverId = UUID.randomUUID();
        String title = "Title";
        String content = "Content";
        NotificationLevel level = NotificationLevel.WARNING;

        spyService.createAndSendNotification(receiverId, title, content, level);

        ArgumentCaptor<NotificationDto> captor = ArgumentCaptor.forClass(NotificationDto.class);
        verify(spyService).sendNotification(captor.capture());
        NotificationDto dto = captor.getValue();
        assertEquals(receiverId, dto.receiverId());
        assertEquals(title, dto.title());
        assertEquals(content, dto.content());
        assertEquals(level, dto.level());
        assertNotNull(dto.id());
        assertNotNull(dto.createdAt());
    }
}
