package com.part4.team05.sb01otbooteam05.domain.directMessage.controller;

import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.directMessage.dto.DirectMessageDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.directMessage.service.DirectMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/direct-messages")
@RequiredArgsConstructor
public class DirectMessageRestController {

    private final DirectMessageService directMessageService;

    @GetMapping
    public ResponseEntity<DirectMessageDtoCursorResponse> getMessages(
            @RequestParam UUID opponentId,
            @RequestParam(required = false) UUID idAfter,
            @RequestParam(defaultValue = "20") int limit
            ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        UUID currentUserId = userDetails.getUserId();

        return ResponseEntity.ok(
                directMessageService.getMessages(currentUserId, opponentId, idAfter, limit)
        );
    }
}
