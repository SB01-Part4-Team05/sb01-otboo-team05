package com.part4.team05.sb01otbooteam05.domain.auth.security.filter;

import com.part4.team05.sb01otbooteam05.domain.auth.security.jwt.JwtTokenProvider;
import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;


  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    try {
      if (SecurityContextHolder.getContext().getAuthentication() != null &&
          SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
        filterChain.doFilter(request, response);
        return;
      }

      String jwt = getJwtFromRequest(request);

      if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
        UUID userId = jwtTokenProvider.getUserIdFromToken(jwt);
        String email = jwtTokenProvider.getEmailFromToken(jwt);
        String role = jwtTokenProvider.getRoleFromToken(jwt);

        CustomUserDetails userDetails = new CustomUserDetails(userId, email, role);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );

        log.info("Authentication success! User: {}, Authorities: {}",
            userDetails.getUsername(), userDetails.getAuthorities());

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

      } else if (StringUtils.hasText(jwt)) {
        log.error("JWT validation failed for token: {}", jwt);
      }
    } catch (Exception ex) {
      log.error("Security Context에 사용자 인증을 설정할 수 없습니다", ex);
    }

    filterChain.doFilter(request, response);
  }

  // 요청에서 JWT 토큰 추출
  private String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
