package com.part4.team05.sb01otbooteam05.domain.notification.controller;

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
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<NotificationDtoCursorResponse> getNotifications(
            @RequestParam(name = "idAfter", required = false) UUID idAfter,
            @RequestParam(name = "limit", defaultValue = "5") @Min(1) @Max(50) int limit,
            @RequestHeader("Authorization") String authorizationHeader
            ) {

        UUID userId = extractUserId(authorizationHeader);
        User user = userService.getUserEntityByIdOrThrow(userId);

        log.info("알림 조회 API 호출: userId={}, limit={}, idAfter={}", userId, limit, idAfter);

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
    public void markAsRead(@PathVariable UUID notificationId,
                           @RequestHeader("Authorization") String authorizationHeader) {
        UUID userId = extractUserId(authorizationHeader);
        notificationService.markAsRead(notificationId, userId);
    }

    private UUID extractUserId(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new OtbooException(ErrorCode.UNAUTHORIZED);
        }
        String token = authorizationHeader.substring(7).trim();
        return jwtTokenProvider.getUserIdFromToken(token);
    }
}
