package com.part4.team05.sb01otbooteam05.domain.auth.service;

import com.part4.team05.sb01otbooteam05.domain.auth.config.JwtProperties;
import com.part4.team05.sb01otbooteam05.domain.auth.dto.SignInRequest;
import com.part4.team05.sb01otbooteam05.domain.auth.dto.SignInResponse;
import com.part4.team05.sb01otbooteam05.domain.auth.dto.TokenRefreshRequest;
import com.part4.team05.sb01otbooteam05.domain.auth.entity.RefreshToken;
import com.part4.team05.sb01otbooteam05.domain.auth.exception.InvalidTokenException;
import com.part4.team05.sb01otbooteam05.domain.auth.exception.UnauthorizedException;
import com.part4.team05.sb01otbooteam05.domain.auth.repository.RefreshTokenRepository;
import com.part4.team05.sb01otbooteam05.domain.auth.security.jwt.JwtTokenProvider;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.entity.UserRole;
import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("인증 서비스 핵심 테스트")
class AuthServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private RefreshTokenRepository refreshTokenRepository;
  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtProperties jwtProperties;
  @Mock private EmailService emailService;
  @InjectMocks private AuthServiceImpl authService;

  private User testUser;
  private final UUID TEST_USER_ID = UUID.randomUUID();
  private final String TEST_EMAIL = "test@example.com";
  private final String TEST_PASSWORD = "password123";

  @BeforeEach
  void setUp() {
    testUser = User.builder()
        .email(TEST_EMAIL)
        .name("Test User")
        .password("encodedPassword")
        .role(UserRole.USER)
        .locked(false)
        .provider("LOCAL")
        .build();
    ReflectionTestUtils.setField(testUser, "id", TEST_USER_ID);
  }

  @Test
  @DisplayName("로그인 성공")
  void signIn_Success() {
    SignInRequest request = new SignInRequest(TEST_EMAIL, TEST_PASSWORD);
    when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(TEST_PASSWORD, "encodedPassword")).thenReturn(true);
    when(refreshTokenRepository.existsByUserIdAndRevokedFalse(TEST_USER_ID)).thenReturn(false);
    when(jwtTokenProvider.createAccessToken(testUser)).thenReturn("accessToken");
    when(jwtTokenProvider.createRefreshToken()).thenReturn("refreshToken");
    when(jwtProperties.getRefreshTokenExpiration()).thenReturn(86400000L);

    SignInResponse response = authService.signIn(request);

    assertThat(response.getAccessToken()).isEqualTo("accessToken");
    assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
    verify(refreshTokenRepository).save(any(RefreshToken.class));
  }

  @Test
  @DisplayName("로그인 실패 - 잘못된 비밀번호")
  void signIn_InvalidPassword() {
    SignInRequest request = new SignInRequest(TEST_EMAIL, TEST_PASSWORD);
    when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(TEST_PASSWORD, "encodedPassword")).thenReturn(false);

    assertThatThrownBy(() -> authService.signIn(request))
        .isInstanceOf(UnauthorizedException.class);
  }

  @Test
  @DisplayName("토큰 재발급 성공")
  void refreshToken_Success() {
    RefreshToken refreshToken = RefreshToken.builder()
        .user(testUser)
        .token("refreshToken")
        .expiresAt(LocalDateTime.now().plusDays(7))
        .revoked(false)
        .build();

    TokenRefreshRequest request = new TokenRefreshRequest("refreshToken");
    when(refreshTokenRepository.findByTokenWithUser("refreshToken")).thenReturn(Optional.of(refreshToken));
    when(jwtTokenProvider.createAccessToken(testUser)).thenReturn("newAccessToken");
    when(jwtTokenProvider.createRefreshToken()).thenReturn("newRefreshToken");
    when(jwtProperties.getRefreshTokenExpiration()).thenReturn(86400000L);

    SignInResponse response = authService.refreshToken(request);

    assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
    assertThat(response.getRefreshToken()).isEqualTo("newRefreshToken");
    verify(refreshTokenRepository).save(any(RefreshToken.class));
  }

  @Test
  @DisplayName("토큰 재발급 실패 - 토큰 없음")
  void refreshToken_TokenNotFound() {
    TokenRefreshRequest request = new TokenRefreshRequest("invalidToken");
    when(refreshTokenRepository.findByTokenWithUser("invalidToken")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.refreshToken(request))
        .isInstanceOf(InvalidTokenException.class);
  }

  @Test
  @DisplayName("비밀번호 초기화 성공")
  void resetPassword_Success() {
    when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
    when(passwordEncoder.encode(anyString())).thenReturn("encodedTempPassword");

    authService.resetPassword(TEST_EMAIL);

    verify(userRepository).save(testUser);
    verify(emailService).sendTempPassword(eq(TEST_EMAIL), anyString(), any(LocalDateTime.class));
  }
}
