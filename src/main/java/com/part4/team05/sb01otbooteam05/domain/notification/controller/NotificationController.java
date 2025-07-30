package com.part4.team05.sb01otbooteam05.domain.notification.controller;

import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.auth.security.jwt.JwtTokenProvider;
import com.part4.team05.sb01otbooteam05.domain.notification.dto.NotificationDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.notification.service.NotificationService;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.service.UserService;
import com.part4.team05.sb01otbooteam05.exception.ErrorCode;
import com.part4.team05.sb01otbooteam05.exception.OtbooException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Validated
@Slf4j
public class NotificationController implements NotificationControllerDoc{

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<NotificationDtoCursorResponse> getNotifications(
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "idAfter", required = false) UUID idAfter,
            @RequestParam(name = "limit", required = true) @Min(1) @Max(50) int limit,
            @AuthenticationPrincipal CustomUserDetails me
            ) {

        UUID userId = me.getUserId();
        User user = userService.getUserEntityByIdOrThrow(userId);

        log.info("알림 조회 API 호출: userId={}, cursor={} idAfter={}, limit={}", userId, cursor, idAfter, limit);

        NotificationDtoCursorResponse response = notificationService.getNotifications(user, cursor, idAfter, limit);

        log.info("알림 응답 전송: size={}, nextCursor={}, hasNext={}",
                response.data().size(),
                response.nextCursor(),
                response.hasNext()
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{notificationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(@PathVariable UUID notificationId,
                           @AuthenticationPrincipal CustomUserDetails me) {
        UUID userId = me.getUserId();
        notificationService.markAsRead(notificationId, userId);
    }
}
