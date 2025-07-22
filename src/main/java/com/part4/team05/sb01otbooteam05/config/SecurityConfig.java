package com.part4.team05.sb01otbooteam05.config;

import static org.springframework.security.config.Customizer.withDefaults;
import com.part4.team05.sb01otbooteam05.domain.auth.security.filter.JwtAuthenticationFilter;

import com.part4.team05.sb01otbooteam05.domain.auth.security.handler.OAuth2AuthenticationFailureHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import com.part4.team05.sb01otbooteam05.domain.auth.security.handler.OAuth2AuthenticationSuccessHandler;
import com.part4.team05.sb01otbooteam05.domain.user.service.CustomOAuth2UserService;
import com.part4.team05.sb01otbooteam05.domain.auth.service.CustomOidcUserService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
@EnableConfigurationProperties(AppOAuth2Properties.class)
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final CustomOAuth2UserService customOAuth2UserService;
  private final CustomOidcUserService customOidcUserService; // 추가
  private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
  private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
    requestHandler.setCsrfRequestAttributeName(null);
    CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
    csrfTokenRepository.setCookieName("XSRF-TOKEN");
    csrfTokenRepository.setHeaderName("X-XSRF-TOKEN");
    http
        // CSRF 설정 추가
        .cors(withDefaults())
        .csrf(csrf -> csrf
            .csrfTokenRepository(csrfTokenRepository)
            .csrfTokenRequestHandler(requestHandler)
            //겟요청인 애들
            .ignoringRequestMatchers(request ->
                "/api/clothes/attribute-defs".equals(request.getRequestURI()) && "GET".equals(request.getMethod())
            )
            .ignoringRequestMatchers(request ->
                "/api/feeds".equals(request.getRequestURI()) && "GET".equals(request.getMethod())
            )
            .ignoringRequestMatchers(request ->
                "/api/users".equals(request.getRequestURI()) && "GET".equals(request.getMethod())
            )
            .ignoringRequestMatchers(request ->
                "/api/clothes".equals(request.getRequestURI()) && "GET".equals(request.getMethod())
            )
            .ignoringRequestMatchers(
                "/api/auth/sign-in",      // 로그인 (초기 인증이므로 CSRF 토큰이 없음)
                "/api/auth/refresh",      // 토큰 재발급
                "/api/auth/me",          // 엑세스 토큰 조회
                "/api/auth/csrf-token",  // CSRF 토큰 조회 (토큰을 받기 위한 요청이므로 제외)
                "/api/auth/reset-password", //비번 초기화
                "/",
                "/index.html", // 기본 페이지 및 아이콘
                "/assets/**",       // 프론트엔드
                "/uploads/**", //사진
                "/images/**", //옷 이미지 위치
                "/api/direct-messages",
                "/api/feeds/*/comments",
                "/api/notifications",
                "/api/users/*/profiles",
                "/api/follows/summary",
                "/api/follows/followings",
                "/api/follows/followers",
                "/api/clothes/extractions",
                "/api/recommendations",
                "/api/weathers",
                "/api/weathers/location",
                "/api/sse",
                "/ws/**"
            )
            .ignoringRequestMatchers(request ->
                "/api/users".equals(request.getRequestURI()) &&
                    "POST".equals(request.getMethod()))  // 코드래빗 참고
        )
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint((request, response, authException) -> {
              String requestURI = request.getRequestURI();

              // WebSocket 관련 요청은 401 응답 (리다이렉트 하지 않음)
              if (requestURI.startsWith("/ws/") || requestURI.equals("/ws")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized WebSocket connection");
                return;
              }

              // API 요청은 JSON 응답
              if (requestURI.startsWith("/api/")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                return;
              }

              // 일반 페이지 요청만 로그인 페이지로 리다이렉트
              response.sendRedirect("/sign-in");
            })
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/api/auth/**",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/",
                "/index.html",
                "/assets/**",
                "/uploads/**",
                "/sign-in",         // 로그인 페이지 허용 추가
                "/sign-up",
                "/login/oauth2/**",
                "/oauth2/**",
                "/ws/**",
                "/*.png",
                "/*.ico",
                "/images/**",
                "/error",
                "/actuator/prometheus" // 도커로 배포 시 바꿔야 함 (보안이슈)
            ).permitAll()
            .requestMatchers(HttpMethod.POST, "/api/users").permitAll()

            // ADMIN 여부 확인
            .requestMatchers("/api/weathers/location").authenticated()
            .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PATCH, "/api/users/{userId}/role").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PATCH, "/api/users/{userId}/lock").hasRole("ADMIN")

            // 위에서 지정하지 않은 나머지 모든 요청은 인증만 되면 접근 가능
            .anyRequest().authenticated()
        )
        .oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(userInfo -> userInfo
                .userService(customOAuth2UserService) // 일반 OAuth2용
                .oidcUserService(customOidcUserService) // OIDC용 (Google)
            )
            .successHandler(oAuth2AuthenticationSuccessHandler)
            .failureHandler(oAuth2AuthenticationFailureHandler)
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
