package com.part4.team05.sb01otbooteam05.domain.auth.controller;

import com.part4.team05.sb01otbooteam05.domain.auth.dto.CsrfTokenDto;
import com.part4.team05.sb01otbooteam05.domain.auth.dto.ResetPasswordRequest;
import com.part4.team05.sb01otbooteam05.domain.auth.dto.SignInRequest;
import com.part4.team05.sb01otbooteam05.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "인증 관리", description = "인증 관련 API")
public interface AuthControllerDoc {

  @Operation(summary = "로그인", description = "이메일과 비밀번호를 입력하여 로그인합니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "로그인 성공",
          content = @Content(schema = @Schema(implementation = String.class))
      ),
      @ApiResponse(
          responseCode = "401",
          description = "로그인 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<String> signIn(@Valid @RequestBody SignInRequest request, HttpServletResponse response);

  @Operation(
      summary = "로그아웃",
      description = "로그아웃합니다.",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
      @ApiResponse(
          responseCode = "401",
          description = "로그아웃 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<Void> signOut(HttpServletResponse response);

  @Operation(summary = "토큰 재발급", description = "쿠키(refresh_token)에 저장된 리프레시 토큰으로 리프레시 토큰과 엑세스 토큰을 재발급합니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "토큰 재발급 성공",
          content = @Content(schema = @Schema(implementation = String.class))
      ),
      @ApiResponse(
          responseCode = "401",
          description = "토큰 재발급 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<String> refreshToken(
      @Parameter(name = "refresh_token", description = "리프레시 토큰", required = true)
      @CookieValue(name = "refresh_token") String refreshToken,
      HttpServletResponse response
  );

  @Operation(summary = "엑세스 토큰 조회", description = "쿠키(refresh_token)에 저장된 리프레시 토큰으로 엑세스 토큰을 조회합니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "인증 정보 조회 성공",
          content = @Content(schema = @Schema(implementation = String.class))
      ),
      @ApiResponse(
          responseCode = "401",
          description = "인증 정보 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<String> getAccessToken(
      @Parameter(name = "refresh_token", description = "refresh_token", required = true)
      @CookieValue(name = "refresh_token") String refreshToken
  );

  @Operation(summary = "CSRF 토큰 조회", description = "CSRF 토큰을 조회합니다. 토큰은 쿠키(XSRF-TOKEN)에 저장됩니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "CSRF 토큰 조회 성공",
          content = @Content(schema = @Schema(implementation = CsrfTokenDto.class))
      ),
      @ApiResponse(
          responseCode = "401",
          description = "CSRF 토큰 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<CsrfTokenDto> getCsrfToken(CsrfToken csrfToken);

  @Operation(summary = "비밀번호 초기화", description = "임시 비밀번호로 초기화 후 이메일로 전송합니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "204",
          description = "비밀번호 초기화 성공"
      ),
      @ApiResponse(
          responseCode = "404",
          description = "비밀번호 초기화 실패(사용자 없음)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  void resetPassword(@Valid @RequestBody ResetPasswordRequest request);
}
