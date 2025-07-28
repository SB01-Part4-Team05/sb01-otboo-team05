package com.part4.team05.sb01otbooteam05.domain.user.controller;

import com.part4.team05.sb01otbooteam05.domain.user.dto.UserDto;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserLockUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserRoleUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.user.entity.UserRole;
import com.part4.team05.sb01otbooteam05.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "프로필 관리", description = "프로필 관련 API")
public interface AdminControllerDoc {

  @Operation(
      summary = "계정 목록 조회",
      description = "계정 목록 조회 API",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "계정 목록 조회 성공",
          content = @Content(schema = @Schema(implementation = UserDtoCursorResponse.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "계정 목록 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<UserDtoCursorResponse> getUsers(
      @Parameter(description = "cursor") @RequestParam(required = false) String cursor,
      @Parameter(description = "idAfter") @RequestParam(required = false) UUID idAfter,
      @Parameter(description = "limit") @RequestParam(required = false) Integer limit,
      @Parameter(description = "sortBy") @RequestParam(required = false) String sortBy,
      @Parameter(description = "sortDirection") @RequestParam(required = false) String sortDirection,
      @Parameter(description = "emailLike") @RequestParam(required = false) String emailLike,
      @Parameter(description = "roleEqual") @RequestParam(required = false) UserRole roleEqual,
      @Parameter(description = "locked") @RequestParam(required = false) Boolean locked
  );

  @Operation(
      summary = "권한 수정",
      description = "권한 수정 API",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "권한 수정 성공",
          content = @Content(schema = @Schema(implementation = UserDto.class))
      ),
      @ApiResponse(
          responseCode = "404",
          description = "권한 수정 실패(사용자 없음)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<UserDto> updateUserRole(
      @Parameter(description = "userId") @PathVariable UUID userId,
      @Parameter(description = "권한 수정 요청") @Valid @RequestBody UserRoleUpdateRequest request
  );

  @Operation(
      summary = "계정 잠금 상태 변경",
      description = "[어드민 기능] 계정 잠금 상태를 변경합니다.",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "계정 잠금 상태 변경 성공",
          content = @Content(schema = @Schema(implementation = UUID.class))
      ),
      @ApiResponse(
          responseCode = "404",
          description = "계정 잠금 상태 변경 실패(사용자 없음)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<UUID> updateUserLockStatus(
      @Parameter(description = "userId") @PathVariable UUID userId,
      @Parameter(description = "계정 잠금 상태 변경 요청") @Valid @RequestBody UserLockUpdateRequest request
  );
}
