package com.part4.team05.sb01otbooteam05.domain.auth.service;

import com.part4.team05.sb01otbooteam05.domain.auth.config.JwtProperties;
import com.part4.team05.sb01otbooteam05.domain.auth.dto.SignInRequest;
import com.part4.team05.sb01otbooteam05.domain.auth.dto.SignInResponse;
import com.part4.team05.sb01otbooteam05.domain.auth.dto.TokenRefreshRequest;
import com.part4.team05.sb01otbooteam05.domain.auth.entity.RefreshToken;
import com.part4.team05.sb01otbooteam05.domain.auth.security.jwt.JwtTokenProvider;
import com.part4.team05.sb01otbooteam05.domain.auth.exception.InvalidTokenException;
import com.part4.team05.sb01otbooteam05.domain.auth.exception.UnauthorizedException;
import com.part4.team05.sb01otbooteam05.domain.auth.repository.RefreshTokenRepository;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final PasswordEncoder passwordEncoder;
  private final JwtProperties jwtProperties;

  /**
   * 로그인
   */
  @Override
  @Transactional
  public SignInResponse signIn(SignInRequest request) {
    log.info("로그인 시도: email={}", request.email());

    // 사용자 조회
    User user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> new UnauthorizedException());

    // 비밀번호 검증
    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new UnauthorizedException();
    }

    // 계정 잠금 확인
    if (user.isLocked()) {
      throw new UnauthorizedException();
    }

    // 기존 로그인된 계정이 있을 경우 강제 로그아웃 처리
    boolean hasExistingLogin = refreshTokenRepository.existsByUserIdAndRevokedFalse(user.getId());
    if (hasExistingLogin) {
      log.info("기존 로그인 세션 발견, 강제 로그아웃 처리: userId={}", user.getId());
      refreshTokenRepository.revokeAllByUserId(user.getId());
    }

    // 토큰 생성
    String accessToken = jwtTokenProvider.createAccessToken(user);
    String refreshTokenValue = jwtTokenProvider.createRefreshToken();

    // 리프레시 토큰 저장
    RefreshToken refreshToken = RefreshToken.builder()
        .user(user)
        .token(refreshTokenValue)
        .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpiration() / 1000))
        .revoked(false)
        .build();

    refreshTokenRepository.save(refreshToken);

    log.info("로그인 성공: userId={}, email={}, 강제로그아웃={}",
        user.getId(), user.getEmail(), hasExistingLogin);

    return SignInResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshTokenValue)
        .userId(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .role(user.getRole().name())
        .build();
  }

  /**
   * 로그아웃
   */
  @Override
  @Transactional
  public void signOut(UUID userId) {
    log.info("로그아웃: userId={}", userId);

    // 해당 사용자의 모든 리프레시 토큰 무효화
    refreshTokenRepository.revokeAllByUserId(userId);
  }

  /**
   * 토큰 재발급 (리프레시 토큰 + 액세스 토큰 모두 재발급)
   */
  @Override
  @Transactional
  public SignInResponse refreshToken(TokenRefreshRequest request) {
    log.info("리프레시 토큰과 액세스 토큰 모두 재발급 요청");

    // 리프레시 토큰 조회
    RefreshToken refreshToken = refreshTokenRepository.findByTokenWithUser(request.refreshToken())
        .orElseThrow(() -> new InvalidTokenException());

    // 토큰 유효성 확인
    if (!refreshToken.isValid()) {
      throw new InvalidTokenException();
    }

    User user = refreshToken.getUser();

    // 기존 리프레시 토큰 무효화
    refreshToken.revoke();

    // 새 토큰들 생성
    String newAccessToken = jwtTokenProvider.createAccessToken(user);
    String newRefreshTokenValue = jwtTokenProvider.createRefreshToken();

    // 새 리프레시 토큰 DB 저장
    RefreshToken newRefreshToken = RefreshToken.builder()
        .user(user)
        .token(newRefreshTokenValue)
        .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpiration() / 1000))
        .revoked(false)
        .build();

    refreshTokenRepository.save(newRefreshToken);

    log.info("토큰 모두 재발급 완료: userId={}, 기존토큰무효화=true", user.getId());

    return SignInResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshTokenValue)
        .userId(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .role(user.getRole().name())
        .build();
  }

  /**
   * 액세스 토큰만 조회
   */
  @Override
  @Transactional(readOnly = true)
  public String getAccessTokenOnly(TokenRefreshRequest request) {
    log.info("액세스 토큰만 조회 요청");

    // 리프레시 토큰 조회
    RefreshToken refreshToken = refreshTokenRepository.findByTokenWithUser(request.refreshToken())
        .orElseThrow(() -> new InvalidTokenException());

    // 토큰 유효성 확인
    if (!refreshToken.isValid()) {
      throw new InvalidTokenException();
    }

    User user = refreshToken.getUser();

    // 새 액세스 토큰만 생성 (리프레시 토큰은 그대로)
    String newAccessToken = jwtTokenProvider.createAccessToken(user);

    log.info("액세스 토큰 조회 완료: userId={}, 리프레시토큰유지=true", user.getId());

    return newAccessToken;
  }

}
