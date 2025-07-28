package com.part4.team05.sb01otbooteam05.domain.user.controller;

import com.part4.team05.sb01otbooteam05.domain.user.dto.ChangePasswordRequest;
import com.part4.team05.sb01otbooteam05.domain.user.dto.ProfileDto;
import com.part4.team05.sb01otbooteam05.domain.user.dto.ProfileUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserDto;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(name = "프로필 관리", description = "프로필 관련 API")
public interface UserControllerDoc {

  @Operation(
      summary = "사용자 등록(회원가입)",
      description = "사용자 등록(회원가입) API"
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "사용자 등록(회원가입) 성공",
          content = @Content(schema = @Schema(implementation = UserDto.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "사용자 등록(회원가입) 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<UserDto> signUp(
      @Parameter(description = "사용자 등록(회원가입) 요청") @Valid @RequestBody UserCreateRequest request
  );

  @Operation(
      summary = "프로필 조회",
      description = "프로필 조회 API",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "프로필 조회 성공",
          content = @Content(schema = @Schema(implementation = ProfileDto.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "프로필 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<ProfileDto> getProfile(
      @Parameter(description = "userId") @PathVariable UUID userId
  );

  @Operation(
      summary = "프로필 업데이트",
      description = "프로필 업데이트 API",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "프로필 업데이트 성공",
          content = @Content(schema = @Schema(implementation = ProfileDto.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "프로필 업데이트 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<ProfileDto> updateProfile(
      @Parameter(description = "userId") @PathVariable UUID userId,
      @Parameter(description = "프로필 업데이트 요청",
          content = @Content(schema = @Schema(implementation = ProfileUpdateRequest.class)))
      @RequestPart(value = "request") @Valid ProfileUpdateRequest request,
      @Parameter(description = "프로필 이미지 파일")
      @RequestPart(value = "image", required = false) MultipartFile image
  );

  @Operation(
      summary = "비밀번호 변경",
      description = "비밀번호를 변경합니다.",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "비밀번호 변경 성공"),
      @ApiResponse(
          responseCode = "400",
          description = "비밀번호 변경 실패(잘못된 요청)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      ),
      @ApiResponse(
          responseCode = "404",
          description = "비밀번호 변경 실패(사용자 없음)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  void changePassword(
      @Parameter(description = "userId") @PathVariable UUID userId,
      @Parameter(description = "비밀번호 변경 요청") @RequestBody @Valid ChangePasswordRequest request
  );
}
