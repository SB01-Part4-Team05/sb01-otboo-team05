package com.part4.team05.sb01otbooteam05.domain.directMessage.controller;

import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.directMessage.dto.DirectMessageDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.directMessage.service.DirectMessageService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class DirectMessageRestController {

    private final DirectMessageService directMessageService;

    @GetMapping
    public ResponseEntity<DirectMessageDtoCursorResponse> getMessages(
            @RequestParam("opponentId") UUID opponentId,
            @RequestParam(value = "idAfter", required = false) UUID idAfter,
            @RequestParam(value = "limit", defaultValue = "20")
            @Min(1) @Max(100) int limit,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        // 서비스는 내부에서 id DESC 정렬을 고정으로 처리
        DirectMessageDtoCursorResponse resp = directMessageService.getMessages(
                me.getUserId(), opponentId, idAfter, limit
        );
        return ResponseEntity.ok(resp);
    }
}
