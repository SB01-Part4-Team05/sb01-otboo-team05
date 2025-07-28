package com.part4.team05.sb01otbooteam05.domain.notification.controller;

import com.part4.team05.sb01otbooteam05.domain.auth.security.jwt.JwtTokenProvider;
import com.part4.team05.sb01otbooteam05.domain.notification.service.NotificationService;
import com.part4.team05.sb01otbooteam05.exception.ErrorCode;
import com.part4.team05.sb01otbooteam05.exception.OtbooException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
@Slf4j
public class SseController implements SseControllerDoc{

    private final NotificationService notificationService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestHeader("Authorization") String authorizationHeader,
                                @RequestParam(required = false) UUID lastEventId) {
        UUID userId = extractUserId(authorizationHeader);

        log.info("SSE 연결 시작: userId={}, lastEventId={}", userId, lastEventId);

        try {
            return notificationService.connect(userId, lastEventId);
        } catch (Exception e) {
            log.error("SSE 연결 실패: userId={}", userId, e);
            throw new OtbooException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private UUID extractUserId(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("잘못된 Authorization 헤더");
        }
        String token = authorizationHeader.substring(7);
        return jwtTokenProvider.getUserIdFromToken(token);
    }
}

