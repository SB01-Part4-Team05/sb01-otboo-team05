package com.part4.team05.sb01otbooteam05.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }


  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (아직 시큐리티 구현 전이라)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/api/users/signup",
                "/api/users/login",
                "/api/users/**",
                "/swagger-ui/**",
                "/v3/api-docs/**"
            ).permitAll()   // 회원가입/로그인/Swagger 모두 인증 없이 허용
            .anyRequest().permitAll() // 일단 모두 허용하여 테스트 진행 예정 (토큰 구현 전까지)
        );
    return http.build();
  }
}
