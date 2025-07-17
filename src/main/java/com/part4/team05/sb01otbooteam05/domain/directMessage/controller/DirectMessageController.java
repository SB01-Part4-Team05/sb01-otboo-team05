package com.part4.team05.sb01otbooteam05.domain.directMessage.controller;

import com.part4.team05.sb01otbooteam05.domain.directMessage.dto.DirectMessageCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.directMessage.dto.DirectMessageDto;
import com.part4.team05.sb01otbooteam05.domain.directMessage.service.DirectMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
