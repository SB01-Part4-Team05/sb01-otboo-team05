package com.part4.team05.sb01otbooteam05.domain.auth.security.jwt;

import com.part4.team05.sb01otbooteam05.domain.auth.config.JwtProperties;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    @PostConstruct
    public void init() {
        String secret = jwtProperties.getSecret();
        if (secret != null && !secret.isEmpty()) {
            log.info("JWT Secret Key successfully loaded and configured");
        } else {
            log.error("JWT Secret Key IS NOT LOADED. Check your application.yml file.");
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    // 액세스 토큰 생성
    public String createAccessToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("userId", user.getId().toString())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    // 리프레시 토큰 생성
    public String createRefreshToken() {
        return UUID.randomUUID().toString();
    }

    // 토큰에서 사용자 ID 추출
    public UUID getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return UUID.fromString(claims.getSubject());
    }

    // 토큰에서 사용자 이메일 추출
    public String getEmailFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("email", String.class);
    }

    // 토큰에서 사용자 권한 추출
    public String getRoleFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("role", String.class);
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("잘못된 JWT 토큰입니다", ex);
        } catch (ExpiredJwtException ex) {
            log.error("만료된 JWT 토큰입니다", ex);
        } catch (UnsupportedJwtException ex) {
            log.error("지원하지 않는 JWT 토큰입니다", ex);
        } catch (IllegalArgumentException ex) {
            log.error("JWT 토큰이 비어있습니다", ex);
        }
        return false;
    }

    // 토큰 파싱
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
