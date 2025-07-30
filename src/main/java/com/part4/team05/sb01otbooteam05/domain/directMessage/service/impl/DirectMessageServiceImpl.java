package com.part4.team05.sb01otbooteam05.domain.directMessage.service.impl;

import com.part4.team05.sb01otbooteam05.domain.directMessage.dto.DirectMessageCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.directMessage.dto.DirectMessageDto;
import com.part4.team05.sb01otbooteam05.domain.directMessage.dto.DirectMessageDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.directMessage.entity.DirectMessage;
import com.part4.team05.sb01otbooteam05.domain.directMessage.mapper.DirectMessageMapper;
import com.part4.team05.sb01otbooteam05.domain.directMessage.repository.DirectMessageRepository;
import com.part4.team05.sb01otbooteam05.domain.directMessage.service.DirectMessageService;
import com.part4.team05.sb01otbooteam05.domain.notification.entity.NotificationLevel;
import com.part4.team05.sb01otbooteam05.domain.notification.service.NotificationService;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;
import com.part4.team05.sb01otbooteam05.exception.ErrorCode;
import com.part4.team05.sb01otbooteam05.exception.OtbooException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectMessageServiceImpl implements DirectMessageService {

    private final DirectMessageRepository directMessageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final DirectMessageMapper directMessageMapper;

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


        return directMessageMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DirectMessageDtoCursorResponse getMessages(
            UUID userId,
            String cursor,
            UUID idAfter,
            int limit
    ) {
        // limit 검증
        if (limit <= 0 || limit > 100) {
            throw new OtbooException(ErrorCode.INVALID_PAGINATION_LIMIT);
        }

        // cursor 우선, 없으면 idAfter 사용
        UUID effectiveIdAfter = null;
        if(cursor != null && !cursor.isBlank()) {
            try {
                effectiveIdAfter = UUID.fromString(cursor);
            } catch (IllegalArgumentException ex) {
                throw new OtbooException(ErrorCode.INVALID_PAGINATION_LIMIT);
            }
        } else {
            effectiveIdAfter = idAfter;
        }

        // 페이징 정렬
        PageRequest page = PageRequest.of(0, limit, Sort.by(Direction.DESC, "id"));

        List<DirectMessage> messages = directMessageRepository.findByUserIdAndIdLessThan(
                userId, effectiveIdAfter, page
        );

        List<DirectMessageDto> dtoList = messages.stream()
                .map(directMessageMapper::toDto)
                .toList();

        UUID nextId = dtoList.isEmpty() ? null
                : dtoList.get(dtoList.size() - 1).id();
        String nextCursor = nextId != null ? nextId.toString() : null;

        long total = directMessageRepository.countByUserId(userId);
        boolean hasNext = dtoList.size() == limit;

        return new DirectMessageDtoCursorResponse(
                dtoList,
                nextCursor,
                nextId,
                hasNext,
                total,
                "id",
                "DESC"
        );
    }
}
