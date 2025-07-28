package com.part4.team05.sb01otbooteam05.domain.auth.service;

import com.part4.team05.sb01otbooteam05.domain.auth.config.JwtProperties;
import com.part4.team05.sb01otbooteam05.domain.auth.dto.SignInRequest;
import com.part4.team05.sb01otbooteam05.domain.auth.dto.SignInResponse;
import com.part4.team05.sb01otbooteam05.domain.auth.dto.TokenRefreshRequest;
import com.part4.team05.sb01otbooteam05.domain.auth.entity.RefreshToken;
import com.part4.team05.sb01otbooteam05.domain.auth.exception.InvalidTokenException;
import com.part4.team05.sb01otbooteam05.domain.auth.exception.UnauthorizedException;
import com.part4.team05.sb01otbooteam05.domain.auth.repository.RefreshTokenRepository;
import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.auth.security.jwt.JwtTokenProvider;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.entity.UserRole;
import com.part4.team05.sb01otbooteam05.domain.user.exception.UserNotFoundException;
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
import java.util.Map;

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

  @Test
  @DisplayName("로그인 실패 - 계정 잠금")
  void signIn_AccountLocked() {
    testUser = User.builder()
        .email(TEST_EMAIL)
        .name("Test User")
        .password("encodedPassword")
        .role(UserRole.USER)
        .locked(true)
        .provider("LOCAL")
        .build();

    SignInRequest request = new SignInRequest(TEST_EMAIL, TEST_PASSWORD);
    when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(TEST_PASSWORD, "encodedPassword")).thenReturn(true);

    assertThatThrownBy(() -> authService.signIn(request))
        .isInstanceOf(UnauthorizedException.class);
  }

  @Test
  @DisplayName("로그인 실패 - 사용자 없음")
  void signIn_UserNotFound() {
    SignInRequest request = new SignInRequest(TEST_EMAIL, TEST_PASSWORD);
    when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.signIn(request))
        .isInstanceOf(UnauthorizedException.class);
  }

  @Test
  @DisplayName("로그인 성공 - 기존 세션 강제 로그아웃")
  void signIn_ForceLogoutExistingSession() {
    SignInRequest request = new SignInRequest(TEST_EMAIL, TEST_PASSWORD);
    when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(TEST_PASSWORD, "encodedPassword")).thenReturn(true);
    when(refreshTokenRepository.existsByUserIdAndRevokedFalse(TEST_USER_ID)).thenReturn(true);
    when(jwtTokenProvider.createAccessToken(testUser)).thenReturn("accessToken");
    when(jwtTokenProvider.createRefreshToken()).thenReturn("refreshToken");
    when(jwtProperties.getRefreshTokenExpiration()).thenReturn(86400000L);

    SignInResponse response = authService.signIn(request);

    assertThat(response.getAccessToken()).isEqualTo("accessToken");
    verify(refreshTokenRepository).revokeAllByUserId(TEST_USER_ID);
  }

  @Test
  @DisplayName("임시 비밀번호 만료로 로그인 실패")
  void signIn_TempPasswordExpired() {
    // 임시 비밀번호 사용자 생성
    User tempPasswordUser = User.builder()
        .email(TEST_EMAIL)
        .name("Test User")
        .password("tempPassword")
        .role(UserRole.USER)
        .locked(false)
        .provider("LOCAL")
        .build();
    ReflectionTestUtils.setField(tempPasswordUser, "id", TEST_USER_ID);

    tempPasswordUser.setTempPassword("tempPassword", LocalDateTime.now().minusHours(3));

    SignInRequest request = new SignInRequest(TEST_EMAIL, TEST_PASSWORD);
    when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(tempPasswordUser));
    when(passwordEncoder.matches(TEST_PASSWORD, "tempPassword")).thenReturn(true);

    assertThatThrownBy(() -> authService.signIn(request))
        .isInstanceOf(UnauthorizedException.class);
  }

  @Test
  @DisplayName("로그아웃 성공")
  void signOut_Success() {
    authService.signOut(TEST_USER_ID);

    verify(refreshTokenRepository).revokeAllByUserId(TEST_USER_ID);
  }

  @Test
  @DisplayName("액세스 토큰만 조회 성공")
  void getAccessTokenOnly_Success() {
    RefreshToken refreshToken = RefreshToken.builder()
        .user(testUser)
        .token("refreshToken")
        .expiresAt(LocalDateTime.now().plusDays(7))
        .revoked(false)
        .build();

    TokenRefreshRequest request = new TokenRefreshRequest("refreshToken");
    when(refreshTokenRepository.findByTokenWithUser("refreshToken")).thenReturn(Optional.of(refreshToken));
    when(jwtTokenProvider.createAccessToken(testUser)).thenReturn("newAccessToken");

    String result = authService.getAccessTokenOnly(request);

    assertThat(result).isEqualTo("newAccessToken");
    verify(jwtTokenProvider, never()).createRefreshToken();
  }

  @Test
  @DisplayName("소셜 로그인 사용자 토큰 발급 성공")
  void signInOAuthUser_Success() {
    CustomUserDetails userDetails = new CustomUserDetails(TEST_USER_ID, TEST_EMAIL, "USER", Map.of());
    when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
    when(refreshTokenRepository.existsByUserIdAndRevokedFalse(TEST_USER_ID)).thenReturn(false);
    when(jwtTokenProvider.createAccessToken(testUser)).thenReturn("accessToken");
    when(jwtTokenProvider.createRefreshToken()).thenReturn("refreshToken");
    when(jwtProperties.getRefreshTokenExpiration()).thenReturn(86400000L);

    SignInResponse response = authService.signInOAuthUser(userDetails);

    assertThat(response.getAccessToken()).isEqualTo("accessToken");
    assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
  }

  @Test
  @DisplayName("비밀번호 초기화 실패 - 사용자 없음")
  void resetPassword_UserNotFound() {
    when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.resetPassword(TEST_EMAIL))
        .isInstanceOf(UserNotFoundException.class);
  }
}
