package com.part4.team05.sb01otbooteam05.domain.auth.security.handler;

import com.part4.team05.sb01otbooteam05.config.AppOAuth2Properties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

  private final AppOAuth2Properties appOAuth2Properties;

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException, ServletException {

    String errorMessage = "Social login failed";

    if (exception instanceof OAuth2AuthenticationException) {
      OAuth2AuthenticationException oauthException = (OAuth2AuthenticationException) exception;
      String oauthErrorCode = oauthException.getError().getErrorCode();

      switch (oauthErrorCode) {
        case "email_already_exists":
          errorMessage = "Email already registered with local account";
          break;
        case "email_used_by_other_provider":
          errorMessage = oauthException.getError().getDescription();
          break;
        default:
          errorMessage = "Authentication failed";
      }
    }

    log.error("OAuth2 로그인 실패: {}", errorMessage, exception);

    String targetUrl = appOAuth2Properties.getRedirect().getFailureUrl();
    getRedirectStrategy().sendRedirect(request, response, targetUrl);

  }
}
