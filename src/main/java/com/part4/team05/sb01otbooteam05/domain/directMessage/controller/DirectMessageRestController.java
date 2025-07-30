package com.part4.team05.sb01otbooteam05.domain.directMessage.controller;

import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.directMessage.dto.DirectMessageDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.directMessage.service.DirectMessageService;
import com.part4.team05.sb01otbooteam05.exception.ErrorCode;
import com.part4.team05.sb01otbooteam05.exception.OtbooException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/direct-messages")
@RequiredArgsConstructor
@Validated
public class DirectMessageRestController implements DirectMessageRestControllerDoc{

    private final DirectMessageService directMessageService;

    @GetMapping
    public ResponseEntity<DirectMessageDtoCursorResponse> getMessages(
            @RequestParam("userId") @NotNull UUID userId,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "idAfter", required = false) UUID idAfter,
            @RequestParam("limit") int limit,
            @AuthenticationPrincipal CustomUserDetails me
    ) {

        // limit 검증
        if (limit < 1 || limit > 100) {
            throw new OtbooException(ErrorCode.INVALID_PAGINATION_LIMIT);
        }

        // 본인 확인
        if (!me.getUserId().equals(userId)) {
            throw new OtbooException(ErrorCode.DM_SENDER_MISMATCH);
        }

        DirectMessageDtoCursorResponse resp = directMessageService.getMessages(
                userId, cursor, idAfter, limit
        );
        return ResponseEntity.ok(resp);
    }
}
