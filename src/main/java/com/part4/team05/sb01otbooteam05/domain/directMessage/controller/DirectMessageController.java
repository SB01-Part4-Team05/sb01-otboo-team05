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
import org.springframework.stereotype.Controller;

import java.security.Principal;
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
    public void send(
            DirectMessageCreateRequest request,
            Principal principal    // ChannelInterceptor에서 setUser(auth) 해둔 Authentication
    ) {
        // 1) 빈 메시지면 무시
        if (request.content() == null || request.content().trim().isEmpty()) {
            log.debug("빈 메시지 요청 무시");
            return;
        }

        // 2) principal에서 꺼낸 실제 로그인 사용자 ID만 사용
        UUID actualSenderId = ((CustomUserDetails)
                ((org.springframework.security.core.Authentication) principal).getPrincipal()
        ).getUserId();

        // 3) 클라이언트가 보낸 request.senderId는 무시하고, 항상 actualSenderId로 재구성
        DirectMessageCreateRequest safeReq = new DirectMessageCreateRequest(
                actualSenderId,
                request.receiverId(),
                request.content()
        );

        // 4) 서비스 호출
        DirectMessageDto saved = directMessageService.sendMessage(safeReq);

        // 5) DM Key 만들고 발행
        String dmKey = Stream.of(actualSenderId.toString(), request.receiverId().toString())
                .sorted()
                .collect(Collectors.joining("_"));
        messagingTemplate.convertAndSend("/sub/direct-messages_" + dmKey, saved);

        log.info("DM 전송 완료: sender={}, receiver={}", actualSenderId, request.receiverId());
    }

    private String generateDmKey(UUID id1, UUID id2) {
        return Stream.of(id1.toString(), id2.toString())
                .sorted()
                .collect(Collectors.joining("_"));
    }
}
