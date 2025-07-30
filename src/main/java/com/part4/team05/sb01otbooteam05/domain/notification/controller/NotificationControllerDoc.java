package com.part4.team05.sb01otbooteam05.domain.notification.controller;

import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.notification.dto.NotificationDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "알림", description = "알림 API")
public interface NotificationControllerDoc {

  @Operation(
      summary = "알림 목록 조회",
      description = "알림 목록 조회 API",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "알림 목록 조회 성공",
          content = @Content(schema = @Schema(implementation = NotificationDtoCursorResponse.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "알림 목록 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<NotificationDtoCursorResponse> getNotifications(
          @Parameter(description = "커서 토큰", example = "eyJpZCI6IjEyMyJ9")
          @RequestParam(name = "cursor", required = false)
          String cursor,

          @Parameter(description = "이전 마지막 알림 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
          @RequestParam(name = "idAfter", required = false)
          UUID idAfter,

          @Parameter(description = "한 페이지당 최대 개수", example = "5")
          @RequestParam(name = "limit", required = true) @Min(1) @Max(50)
          int limit,

          @Parameter(hidden = true)
          @AuthenticationPrincipal
          CustomUserDetails me
  );

  @Operation(
      summary = "알림 읽음 처리",
      description = "알림 읽음 처리 API",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "알림 읽음 처리 성공"),
      @ApiResponse(
          responseCode = "400",
          description = "알림 읽음 처리 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  void markAsRead(
      @Parameter(description = "notificationId") @PathVariable UUID notificationId,
      @Parameter(hidden = true)
      @AuthenticationPrincipal CustomUserDetails me
  );
}
