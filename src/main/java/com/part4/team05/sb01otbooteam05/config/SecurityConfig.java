package com.part4.team05.sb01otbooteam05.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import com.part4.team05.sb01otbooteam05.domain.auth.security.filter.JwtAuthenticationFilter; // 이 import 추가 필요


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public PasswordEncoder passwordEncoder() { //비밀번호 암호화
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // CSRF 설정 추가
        .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .ignoringRequestMatchers("/api/auth/**")
            .ignoringRequestMatchers(request ->
                "/api/users".equals(request.getRequestURI()) &&
                    "POST".equals(request.getMethod()))  // 코드래빗 참고
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.POST, "/api/users").permitAll()  // 회원가입만 허용하기 위한 post만 허용
            .requestMatchers(
                "/api/auth/sign-in",        // 로그인 허용
                "/api/auth/refresh",       // 토큰 재발급 허용
                "/api/auth/csrf-token",
                "/api/auth/me",
                "/swagger-ui/**",
                "/v3/api-docs/**"
            ).permitAll()
            //.anyRequest().permitAll() // 이거는 일시적으로 모두 접근 허용하는 것 (테스트용)
            .anyRequest().authenticated() // 나머지는 토큰 필수 (다른 도메인도 인증 추가하여 구현 진행해야함!)
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
