package com.part4.team05.sb01otbooteam05.domain.directMessage.controller;

import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.directMessage.dto.DirectMessageCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.directMessage.dto.DirectMessageDto;
import com.part4.team05.sb01otbooteam05.domain.directMessage.service.DirectMessageService;
import com.part4.team05.sb01otbooteam05.exception.ErrorCode;
import com.part4.team05.sb01otbooteam05.exception.OtbooException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Controller
public class DirectMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final DirectMessageService directMessageService;

    @MessageMapping("/direct-messages_send")
    public void send(@Valid DirectMessageCreateRequest request) {
        // 현재 로그인한 사용자 정보
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID authenticatedUserId = userDetails.getUserId();

        // 클라이언트에서 남긴 senderId와 일치하는지 확인
        if (!authenticatedUserId.equals(request.senderId())) {
            log.warn("인증된 사용자와 senderId 불일치! senderId={}, authenticated={}",
                    request.senderId(), authenticatedUserId);
            throw new OtbooException(ErrorCode.DM_SENDER_MISMATCH);
        }

        DirectMessageDto saved = directMessageService.sendMessage(request);
        String dmKey = generateDmKey(request.senderId(), request.receiverId());
        messagingTemplate.convertAndSend("/sub/direct-messages_" + dmKey, saved);

        log.info("DM 전송 완료: sender={}, receiver={}", request.senderId(), request.receiverId());
    }

    private String generateDmKey(UUID id1, UUID id2) {
        return Stream.of(id1.toString(), id2.toString())
                .sorted()
                .collect(Collectors.joining("_"));
    }
}
