package com.part4.team05.sb01otbooteam05.domain.notification.controller;

import com.part4.team05.sb01otbooteam05.domain.notification.dto.NotificationDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.notification.service.NotificationService;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.service.UserService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Validated
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<NotificationDtoCursorResponse> getNotifications(
            @RequestParam(name = "idAfter", required = false) UUID idAfter,
            @RequestParam(name = "limit", defaultValue = "5") @Min(1) @Max(50) int limit,
            @RequestHeader("X-USER-ID") UUID userId // 인증 안되니까 헤더로 임시 처리
            ) {
        log.info("알림 조회 API 호출: userId={}, limit={}, idAfter={}", userId, limit, idAfter);

        User user = userService.getUserEntityByIdOrThrow(userId);

        NotificationDtoCursorResponse response = notificationService.getNotifications(user, idAfter, limit);

        log.info("알림 응답 전송: size={}, nextCursor={}, hasNext={}",
                response.data().size(),
                response.nextCursor(),
                response.hasNext()
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{notificationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(@PathVariable UUID notificationId) {
        notificationService.markAsRead(notificationId);
    }
}
