package com.part4.team05.sb01otbooteam05.domain.directMessage.service.impl;

import com.part4.team05.sb01otbooteam05.domain.directMessage.dto.DirectMessageCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.directMessage.dto.DirectMessageDto;
import com.part4.team05.sb01otbooteam05.domain.directMessage.dto.DirectMessageDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.directMessage.entity.DirectMessage;
import com.part4.team05.sb01otbooteam05.domain.directMessage.repository.DirectMessageRepository;
import com.part4.team05.sb01otbooteam05.domain.directMessage.service.DirectMessageService;
import com.part4.team05.sb01otbooteam05.domain.notification.entity.NotificationLevel;
import com.part4.team05.sb01otbooteam05.domain.notification.service.NotificationService;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserSummary;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;
import com.part4.team05.sb01otbooteam05.exception.ErrorCode;
import com.part4.team05.sb01otbooteam05.exception.OtbooException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectMessageServiceImpl implements DirectMessageService {

    private final DirectMessageRepository directMessageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public DirectMessageDto sendMessage(DirectMessageCreateRequest request) {
        if (request.content() == null || request.content().trim().isEmpty()) {
            throw new OtbooException(ErrorCode.INVALID_MESSAGE_CONTENT);
        }

        User sender = userRepository.findById(request.senderId())
                .orElseThrow(() -> new OtbooException(ErrorCode.USER_NOT_FOUND));
        User receiver = userRepository.findById(request.receiverId())
                .orElseThrow(() -> new OtbooException(ErrorCode.USER_NOT_FOUND));

        DirectMessage message = new DirectMessage(request.content(), sender, receiver);
        DirectMessage saved = directMessageRepository.save(message);

        // DirectMessage 저장 후 알림 전송
        try {
            notificationService.createAndSendNotification(
                    receiver.getId(),
                    "[DM] " + sender.getName(),
                    message.getContent(),
                    NotificationLevel.INFO
            );
        } catch (Exception e) {
            log.warn("DM 알림 전송 실패: sender={}, receiver={}", sender.getId(), receiver.getId(), e);
        }


        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DirectMessageDtoCursorResponse getMessages(UUID userId1, UUID userId2, UUID idAfter, int limit, String sortBy,
                                                      String direction) {
        if (limit <= 0 || limit > 100) {
            throw new OtbooException(ErrorCode.INVALID_PAGINATION_LIMIT);
        }

        PageRequest pageRequest = PageRequest.of(0, limit);
        List<DirectMessage> messages = directMessageRepository.findMessages(userId1, userId2, idAfter, pageRequest);

        List<DirectMessageDto> dtoList = messages.stream()
                .map(this::toDto)
                .toList();

        UUID nextIdAfter = dtoList.isEmpty() ? null : dtoList.get(dtoList.size() - 1).id();

        long totalCount = directMessageRepository.countByUserPair(userId1, userId2);

        return new DirectMessageDtoCursorResponse(
                dtoList,
                nextIdAfter != null ? nextIdAfter.toString() : null,
                nextIdAfter,
                messages.size() == limit,
                totalCount,
                sortBy,
                direction
        );
    }

    private DirectMessageDto toDto(DirectMessage m) {
        return new DirectMessageDto(
                m.getId(),
                m.getCreatedAt(),
                toUserSummary(m.getSender()),
                toUserSummary(m.getReceiver()),
                m.getContent()
        );
    }

    private UserSummary toUserSummary(User user) {
        return new UserSummary(
                user.getId(),
                user.getName(),
                user.getProfileImageUrl()
        );
    }
}
