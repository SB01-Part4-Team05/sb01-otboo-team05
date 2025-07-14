package com.part4.team05.sb01otbooteam05.config;

import static org.springframework.security.config.Customizer.withDefaults;
import com.part4.team05.sb01otbooteam05.domain.auth.security.filter.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public PasswordEncoder passwordEncoder() { //비밀번호 암호화
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
    requestHandler.setCsrfRequestAttributeName(null);
    http
        // CSRF 설정 추가
        .cors(withDefaults())
        .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .csrfTokenRequestHandler(requestHandler)
            .ignoringRequestMatchers(
                "/api/auth/sign-in",      // 로그인 (초기 인증이므로 CSRF 토큰이 없음)
                "/api/auth/refresh",      // 토큰 재발급
                "/api/auth/me",          // 토큰 조회 (GET 요청이므로 제외)
                "/api/auth/csrf-token",  // CSRF 토큰 조회 (토큰을 받기 위한 요청이므로 제외)
                "/api/auth/reset-password", //비번 초기화
                "/",
                "/index.html", // 기본 페이지 및 아이콘
                "/assets/**",       // 프론트엔드
                "/uploads/**" //사진
                //todo 옷 사진도 여기에 넣어야함!
            )
            .ignoringRequestMatchers(request ->
                "/api/users".equals(request.getRequestURI()) &&
                    "POST".equals(request.getMethod()))  // 코드래빗 참고
        )
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/api/auth/**",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/",
                "/index.html",
                "/assets/**",
                "/uploads/**"
            ).permitAll()
            .requestMatchers(HttpMethod.POST, "/api/users").permitAll()

            // ADMIN 전용 경로들을 여기에 직접 명시
            .requestMatchers("/api/weathers/location").authenticated()
            .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PATCH, "/api/users/{userId}/role").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PATCH, "/api/users/{userId}/lock").hasRole("ADMIN")

            // 위에서 지정하지 않은 나머지 모든 요청은 인증만 되면 접근 가능
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
