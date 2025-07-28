package com.part4.team05.sb01otbooteam05.domain.auth.controller;

import com.part4.team05.sb01otbooteam05.domain.auth.dto.ResetPasswordRequest;
import com.part4.team05.sb01otbooteam05.domain.auth.dto.SignInRequest;
import com.part4.team05.sb01otbooteam05.domain.auth.dto.SignInResponse;
import com.part4.team05.sb01otbooteam05.domain.auth.dto.TokenRefreshRequest;
import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.auth.service.AuthService;
import com.part4.team05.sb01otbooteam05.domain.auth.dto.CsrfTokenDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController implements AuthControllerDoc{

  private final AuthService authService;

  /**
   * 로그인
   */
  @PostMapping("/sign-in")
  public ResponseEntity<String> signIn(@Valid @RequestBody SignInRequest request, HttpServletResponse response) {
    SignInResponse authResponse = authService.signIn(request);

    // refresh_token을 쿠키에 자동 저장
    addRefreshTokenCookie(response, authResponse.getRefreshToken());

    return ResponseEntity.ok(authResponse.getAccessToken());
  }

  /**
   * 로그아웃
   */
  @PostMapping("/sign-out")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> signOut(HttpServletResponse response) {
    // SecurityContextHolder에서 인증 정보 가져오기
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    UUID userId = userDetails.getUserId();

    authService.signOut(userId);

    //로그아웃 시 쿠키 삭제 추가
    Cookie refreshTokenCookie = new Cookie("refresh_token", null);
    refreshTokenCookie.setMaxAge(0);
    refreshTokenCookie.setPath("/");
    response.addCookie(refreshTokenCookie);

    // access_token 쿠키도 삭제
    Cookie accessTokenCookie = new Cookie("access_token", null);
    accessTokenCookie.setMaxAge(0);
    accessTokenCookie.setPath("/");
    response.addCookie(accessTokenCookie);

    return ResponseEntity.noContent().build();
  }

  /**
   * 토큰 재발급 (리프레시 토큰 + 액세스 토큰 모두 새로 발급) (쿠키에 저장된 리프레시를 가지고)
   */
  @PostMapping("/refresh")
  public ResponseEntity<String> refreshToken(
      @CookieValue(name = "refresh_token") String refreshToken,
      HttpServletResponse response) {

    TokenRefreshRequest request = new TokenRefreshRequest(refreshToken);
    SignInResponse authResponse = authService.refreshToken(request);
    //새로운 refresh_token을 쿠키에 업데이트
    addRefreshTokenCookie(response, authResponse.getRefreshToken());

    return ResponseEntity.ok(authResponse.getAccessToken());
  }

  /**
   * 액세스 토큰 조회 (리프레시 토큰은 그대로 유지)
   */
  @GetMapping("/me")
  public ResponseEntity<String> getAccessToken(
      @CookieValue(name = "refresh_token") String refreshToken) {

    TokenRefreshRequest request = new TokenRefreshRequest(refreshToken);
    String accessToken = authService.getAccessTokenOnly(request);
    return ResponseEntity.ok(accessToken);
  }

  /**
   * CSRF 토큰 조회 (신규 추가된 기능)
   */
  @GetMapping("/csrf-token")
  public ResponseEntity<CsrfTokenDto> getCsrfToken(CsrfToken csrfToken) {
    CsrfTokenDto response = CsrfTokenDto.builder()
        .headerName(csrfToken.getHeaderName())     // 헤더명: X-XSRF-TOKEN
        .token(csrfToken.getToken())               // 실제 토큰 값
        .parameterName(csrfToken.getParameterName()) // 쿠키명: XSRF-TOKEN
        .build();

    return ResponseEntity.ok(response);
  }

  /**
   * 리프레시 토큰 쿠키로 추가
   */
  private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
    Cookie cookie = new Cookie("refresh_token", refreshToken);
    cookie.setHttpOnly(true);
    cookie.setSecure(false);    // 개발환경용
    cookie.setPath("/");
    cookie.setMaxAge(7 * 24 * 60 * 60); // 7일
    // cookie.setAttribute("SameSite", "Lax");  // TODO: 추후 주석 제거 예정
    response.addCookie(cookie);
  }

  /**
   * 비밀번호 초기화
   */
  @PostMapping("/reset-password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request.email());
  }
}
