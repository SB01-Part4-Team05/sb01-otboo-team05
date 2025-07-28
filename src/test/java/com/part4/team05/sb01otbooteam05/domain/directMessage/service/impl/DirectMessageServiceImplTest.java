package com.part4.team05.sb01otbooteam05.domain.directMessage.service.impl;

import com.part4.team05.sb01otbooteam05.domain.directMessage.dto.DirectMessageCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.directMessage.dto.DirectMessageDto;
import com.part4.team05.sb01otbooteam05.domain.directMessage.dto.DirectMessageDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.directMessage.entity.DirectMessage;
import com.part4.team05.sb01otbooteam05.domain.directMessage.repository.DirectMessageRepository;
import com.part4.team05.sb01otbooteam05.domain.notification.entity.NotificationLevel;
import com.part4.team05.sb01otbooteam05.domain.notification.service.NotificationService;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;
import com.part4.team05.sb01otbooteam05.exception.ErrorCode;
import com.part4.team05.sb01otbooteam05.exception.OtbooException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DirectMessageServiceImplTest {

    @Mock DirectMessageRepository directMessageRepository;
    @Mock UserRepository userRepository;
    @Mock NotificationService notificationService;
    @InjectMocks DirectMessageServiceImpl service;

    UUID senderId   = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();
    String content  = "Hello, world!";

    @Nested
    class SendMessageTests {

        UUID senderId   = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        @Test
        void successSendsNotification() {
            // given
            DirectMessageCreateRequest req = new DirectMessageCreateRequest(senderId, receiverId, content);

            User sender = mock(User.class);
            given(sender.getId()).willReturn(senderId);
            given(sender.getName()).willReturn("Alice");
            given(userRepository.findById(senderId)).willReturn(Optional.of(sender));

            User receiver = mock(User.class);
            given(receiver.getId()).willReturn(receiverId);
            given(userRepository.findById(receiverId)).willReturn(Optional.of(receiver));

            DirectMessage msg = spy(new DirectMessage(content, sender, receiver));
            UUID msgId = UUID.randomUUID();
            doReturn(msgId).when(msg).getId();
            LocalDateTime ts = LocalDateTime.of(2025,1,1,12,0);
            doReturn(ts).when(msg).getCreatedAt();

            given(directMessageRepository.save(any(DirectMessage.class))).willReturn(msg);

            // when
            DirectMessageDto dto = service.sendMessage(req);

            // then DTO
            assertThat(dto.id()).isEqualTo(msgId);
            assertThat(dto.createdAt()).isEqualTo(ts);
            assertThat(dto.sender().userId()).isEqualTo(senderId);
            assertThat(dto.receiver().userId()).isEqualTo(receiverId);
            assertThat(dto.content()).isEqualTo(content);

            // then 알림 호출 (raw 값만 사용)
            String expectedTitle = "[DM] " + sender.getName();  // "Alice"
            then(notificationService).should().createAndSendNotification(
                    receiverId,
                    expectedTitle,
                    content,
                    NotificationLevel.INFO
            );
        }

        @Test
        void notificationExceptionIsCaught() {
            DirectMessageCreateRequest req = new DirectMessageCreateRequest(senderId, receiverId, content);

            User sender = mock(User.class);
            given(userRepository.findById(senderId)).willReturn(Optional.of(sender));
            User receiver = mock(User.class);
            given(userRepository.findById(receiverId)).willReturn(Optional.of(receiver));

            DirectMessage msg = spy(new DirectMessage(content, sender, receiver));
            doReturn(UUID.randomUUID()).when(msg).getId();
            doReturn(LocalDateTime.now()).when(msg).getCreatedAt();
            given(directMessageRepository.save(any())).willReturn(msg);

            willThrow(new RuntimeException("boom"))
                    .given(notificationService)
                    .createAndSendNotification(any(), any(), any(), any());

            assertThatCode(() -> service.sendMessage(req))
                    .doesNotThrowAnyException();
        }

        @Test
        void invalidContent_throws() {
            assertThatThrownBy(() ->
                    service.sendMessage(new DirectMessageCreateRequest(senderId, receiverId, "   "))
            )
                    .isInstanceOf(OtbooException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_MESSAGE_CONTENT);
        }

        @Test
        void userNotFound_sender() {
            given(userRepository.findById(senderId)).willReturn(Optional.empty());
            assertThatThrownBy(() ->
                    service.sendMessage(new DirectMessageCreateRequest(senderId, receiverId, content))
            )
                    .isInstanceOf(OtbooException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test
        void userNotFound_receiver() {
            User sender = mock(User.class);
            given(userRepository.findById(senderId)).willReturn(Optional.of(sender));
            lenient().when(userRepository.findById(receiverId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    service.sendMessage(new DirectMessageCreateRequest(senderId, receiverId, content))
            )
                    .isInstanceOf(OtbooException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test @DisplayName("실패: content가 null이면 INVALID_MESSAGE_CONTENT")
        void sendMessage_invalidContent_null() {
            DirectMessageCreateRequest req = new DirectMessageCreateRequest(senderId, receiverId, null);

            assertThatThrownBy(() -> service.sendMessage(req))
                    .isInstanceOf(OtbooException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_MESSAGE_CONTENT);
        }

        @Test @DisplayName("실패: content가 빈 문자열이면 INVALID_MESSAGE_CONTENT")
        void sendMessage_invalidContent_blank() {
            DirectMessageCreateRequest req = new DirectMessageCreateRequest(senderId, receiverId, "   ");

            assertThatThrownBy(() -> service.sendMessage(req))
                    .isInstanceOf(OtbooException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_MESSAGE_CONTENT);
        }
    }

    @Nested
    class GetMessagesTests {

        @Test
        void partialListHasNoNext() {
            int limit = 5;
            DirectMessage m1 = spy(new DirectMessage(content, mock(User.class), mock(User.class)));
            UUID id1 = UUID.randomUUID();
            doReturn(id1).when(m1).getId();
            doReturn(LocalDateTime.of(2025,1,2,3,4)).when(m1).getCreatedAt();

            PageRequest pr = PageRequest.of(0, limit, Sort.by(Direction.DESC, "id"));
            given(directMessageRepository.findMessages(senderId, receiverId, null, pr))
                    .willReturn(List.of(m1));
            given(directMessageRepository.countByUserPair(senderId, receiverId))
                    .willReturn(7L);

            DirectMessageDtoCursorResponse resp = service.getMessages(
                    senderId, receiverId, null, limit
            );

            assertThat(resp.data()).hasSize(1);
            assertThat(resp.hasNext()).isFalse();
            assertThat(resp.nextCursor()).isEqualTo(id1.toString());
            assertThat(resp.nextIdAfter()).isEqualTo(id1);
            assertThat(resp.totalCount()).isEqualTo(7L);
            assertThat(resp.sortBy()).isEqualTo("id");
            assertThat(resp.sortDirection()).isEqualTo("DESC");
        }

        @Test
        void fullListHasNext() {
            int limit = 2;
            DirectMessage a = spy(new DirectMessage(content, mock(User.class), mock(User.class)));
            DirectMessage b = spy(new DirectMessage(content, mock(User.class), mock(User.class)));
            UUID idA = UUID.randomUUID(), idB = UUID.randomUUID();
            doReturn(idA).when(a).getId();
            doReturn(idB).when(b).getId();
            doReturn(LocalDateTime.now()).when(a).getCreatedAt();
            doReturn(LocalDateTime.now()).when(b).getCreatedAt();

            PageRequest pr = PageRequest.of(0, limit, Sort.by(Direction.DESC, "id"));
            given(directMessageRepository.findMessages(senderId, receiverId, null, pr))
                    .willReturn(List.of(a, b));
            given(directMessageRepository.countByUserPair(senderId, receiverId))
                    .willReturn(10L);

            DirectMessageDtoCursorResponse resp = service.getMessages(
                    senderId, receiverId, null, limit
            );

            assertThat(resp.data()).hasSize(2);
            assertThat(resp.hasNext()).isTrue();
            assertThat(resp.nextCursor()).isEqualTo(idB.toString());
            assertThat(resp.totalCount()).isEqualTo(10L);
            assertThat(resp.sortBy()).isEqualTo("id");
            assertThat(resp.sortDirection()).isEqualTo("DESC");
        }

        @Test
        void invalidLimit_zero() {
            assertThatThrownBy(() ->
                    service.getMessages(senderId, receiverId, null, 0)
            )
                    .isInstanceOf(OtbooException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_PAGINATION_LIMIT);
        }

        @Test
        void invalidLimit_tooLarge() {
            assertThatThrownBy(() ->
                    service.getMessages(senderId, receiverId, null, 101)
            )
                    .isInstanceOf(OtbooException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_PAGINATION_LIMIT);
        }

        @Test
        void emptyListReturnsNoCursor() {
            int limit = 10;
            PageRequest pr = PageRequest.of(0, limit, Sort.by(Direction.DESC, "id"));
            given(directMessageRepository.findMessages(senderId, receiverId, null, pr))
                    .willReturn(List.of());
            given(directMessageRepository.countByUserPair(senderId, receiverId))
                    .willReturn(0L);

            DirectMessageDtoCursorResponse resp = service.getMessages(
                    senderId, receiverId, null, limit
            );

            assertThat(resp.data()).isEmpty();
            assertThat(resp.nextCursor()).isNull();
            assertThat(resp.nextIdAfter()).isNull();
            assertThat(resp.hasNext()).isFalse();
            assertThat(resp.totalCount()).isEqualTo(0L);
            assertThat(resp.sortBy()).isEqualTo("id");
            assertThat(resp.sortDirection()).isEqualTo("DESC");
        }
    }
}
