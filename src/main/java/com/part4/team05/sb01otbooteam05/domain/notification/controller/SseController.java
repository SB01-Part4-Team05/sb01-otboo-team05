package com.part4.team05.sb01otbooteam05.domain.notification.controller;

import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.auth.security.jwt.JwtTokenProvider;
import com.part4.team05.sb01otbooteam05.domain.notification.service.NotificationService;
import com.part4.team05.sb01otbooteam05.exception.ErrorCode;
import com.part4.team05.sb01otbooteam05.exception.OtbooException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
@Slf4j
public class SseController implements SseControllerDoc{

    private final NotificationService notificationService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestParam(name = "LastEventId", required = false) UUID lastEventId
    ) {
        UUID userId = me.getUserId();
        log.info("SSE 연결 시작: userId={}, lastEventId={}", userId, lastEventId);

        SseEmitter emitter;
        try {
            emitter = notificationService.connect(userId, lastEventId);
        } catch (Exception e) {
            log.error("SSE connect 실패: userId={}", userId, e);
            throw new OtbooException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 재연결 시 누락된 알림 전송
        if (lastEventId != null) {
            notificationService.replayMissed(userId, lastEventId, emitter);
        }

        return emitter;
    }
}

