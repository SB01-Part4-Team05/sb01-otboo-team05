package com.part4.team05.sb01otbooteam05.domain.notification.controller;

import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@Tag(name = "sse-controller", description = "Server Sent Event 알림")
public interface SseControllerDoc {

  @Operation(
      summary = "알림 SSE 구독",
      description = "LastEventId 이후 누락된 이벤트를 재전송하며, 실시간 알림 스트림을 시작합니다.",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "OK",
          content = @Content(mediaType = "text/event-stream")
      )
  })
  SseEmitter subscribe(
          @Parameter(hidden = true)
          @AuthenticationPrincipal CustomUserDetails me,

          @Parameter(description = "이전에 수신한 마지막 이벤트 ID")
          @RequestParam(name = "LastEventId", required = false)
          UUID lastEventId
  );
}
