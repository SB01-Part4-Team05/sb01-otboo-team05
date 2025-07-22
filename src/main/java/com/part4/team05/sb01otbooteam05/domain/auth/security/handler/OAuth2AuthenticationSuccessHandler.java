package com.part4.team05.sb01otbooteam05.domain.auth.security.handler;

import com.part4.team05.sb01otbooteam05.config.AppOAuth2Properties;
import com.part4.team05.sb01otbooteam05.domain.auth.dto.SignInResponse;
import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final AuthService authService;
  private final AppOAuth2Properties appOAuth2Properties;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {
    log.info("OAuth2 인증 성공 핸들러 시작");

    try {
      if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
        throw new IllegalStateException("지원하지 않는 Principal 타입입니다: " + authentication.getPrincipal().getClass());
      }

      log.info("CustomUserDetails 직접 사용: userId={}, email={}", userDetails.getUserId(), userDetails.getEmail());

      SignInResponse authResponse = authService.signInOAuthUser(userDetails);
      log.info("토큰 발급 완료");

      addRefreshTokenCookie(response, authResponse.getRefreshToken());
      addAccessTokenCookie(response, authResponse.getAccessToken());

      String targetUrl = appOAuth2Properties.getRedirect().getSuccessUrl();

      log.info("소셜 로그인 성공. 프론트엔드 홈페이지로 리디렉션: {}", targetUrl);
      getRedirectStrategy().sendRedirect(request, response, targetUrl);

    } catch (Exception e) {
      log.error("OAuth2 성공 핸들러에서 오류 발생", e);
      String errorUrl = appOAuth2Properties.getRedirect().getFailureUrl();
      getRedirectStrategy().sendRedirect(request, response, errorUrl + "?error=oauth_failed");
    }
  }

  private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
    Cookie cookie = new Cookie("refresh_token", refreshToken);
    cookie.setHttpOnly(true);
    cookie.setSecure(false);
    cookie.setPath("/");
    cookie.setMaxAge(7 * 24 * 60 * 60); // 7일
    response.addCookie(cookie);
  }

  private void addAccessTokenCookie(HttpServletResponse response, String accessToken) {
    Cookie cookie = new Cookie("access_token", accessToken);
    cookie.setHttpOnly(false);
    cookie.setSecure(false);   // HTTP 환경
    cookie.setPath("/");
    cookie.setMaxAge(30 * 60); // 30분
    response.addCookie(cookie);

    log.info("AccessToken 쿠키 설정 완료");
  }
}
