package com.part4.team05.sb01otbooteam05.domain.auth.service;

import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.entity.UserRole;
import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("구글 OIDC 서비스 핵심 테스트")
class CustomOidcUserServiceTest {

  @Mock private UserRepository userRepository;
  @InjectMocks private CustomOidcUserService customOidcUserService;

  @Test
  @DisplayName("구글 기존 사용자 로그인")
  void processOidcUser_ExistingUser() {
    String testEmail = "test@gmail.com";
    String testName = "Test User";
    String providerId = "google-123";
    UUID testUserId = UUID.randomUUID();

    User testUser = User.builder()
        .email(testEmail)
        .name(testName)
        .role(UserRole.USER)
        .locked(false)
        .provider("GOOGLE")
        .providerId(providerId)
        .build();
    ReflectionTestUtils.setField(testUser, "id", testUserId);

    OidcUserRequest userRequest = mock(OidcUserRequest.class);
    OidcUser oidcUser = mock(OidcUser.class);

    Map<String, Object> attributes = Map.of(
        "sub", providerId,
        "email", testEmail,
        "name", testName
    );

    when(oidcUser.getSubject()).thenReturn(providerId);
    when(oidcUser.getAttributes()).thenReturn(attributes);
    when(oidcUser.getEmail()).thenReturn(testEmail);
    when(oidcUser.getFullName()).thenReturn(testName);
    when(userRepository.findByProviderAndProviderId("GOOGLE", providerId))
        .thenReturn(Optional.of(testUser));

    var result = customOidcUserService.processOidcUser(userRequest, oidcUser);

    assertThat(result).isInstanceOf(CustomUserDetails.class);
    var details = (CustomUserDetails) result;
    assertThat(details.getUserId()).isEqualTo(testUserId);
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("구글 신규 사용자 등록")
  void processOidcUser_NewUser() {
    String testEmail = "test@gmail.com";
    String testName = "Test User";
    String providerId = "google-123";
    UUID testUserId = UUID.randomUUID();

    User testUser = User.builder()
        .email(testEmail)
        .name(testName)
        .role(UserRole.USER)
        .locked(false)
        .provider("GOOGLE")
        .providerId(providerId)
        .build();
    ReflectionTestUtils.setField(testUser, "id", testUserId);

    OidcUserRequest userRequest = mock(OidcUserRequest.class);
    OidcUser oidcUser = mock(OidcUser.class);

    when(oidcUser.getSubject()).thenReturn(providerId);
    when(oidcUser.getEmail()).thenReturn(testEmail);
    when(oidcUser.getFullName()).thenReturn(testName);
    when(userRepository.findByProviderAndProviderId("GOOGLE", providerId))
        .thenReturn(Optional.empty());
    when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    var result = customOidcUserService.processOidcUser(userRequest, oidcUser);

    assertThat(result).isInstanceOf(CustomUserDetails.class);
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("구글 계정 잠금 시 로그인 실패")
  void processOidcUser_AccountLocked() {
    String testEmail = "test@gmail.com";
    String testName = "Test User";
    String providerId = "google-123";

    User lockedUser = User.builder()
        .email(testEmail)
        .locked(true)
        .provider("GOOGLE")
        .providerId(providerId)
        .build();

    OidcUserRequest userRequest = mock(OidcUserRequest.class);
    OidcUser oidcUser = mock(OidcUser.class);

    when(oidcUser.getSubject()).thenReturn(providerId); // 이 부분이 중요!
    when(oidcUser.getEmail()).thenReturn(testEmail);
    when(oidcUser.getFullName()).thenReturn(testName);
    when(userRepository.findByProviderAndProviderId("GOOGLE", providerId))
        .thenReturn(Optional.of(lockedUser));

    assertThatThrownBy(() -> customOidcUserService.processOidcUser(userRequest, oidcUser))
        .isInstanceOf(OAuth2AuthenticationException.class)
        .hasMessageContaining("Account is locked");
  }

  @Test
  @DisplayName("구글 이메일 정보 없음")
  void processOidcUser_MissingEmail() {
    String testName = "Test User";

    OidcUserRequest userRequest = mock(OidcUserRequest.class);
    OidcUser oidcUser = mock(OidcUser.class);

    when(oidcUser.getEmail()).thenReturn(null);
    when(oidcUser.getFullName()).thenReturn(testName);

    assertThatThrownBy(() -> customOidcUserService.processOidcUser(userRequest, oidcUser))
        .isInstanceOf(OAuth2AuthenticationException.class)
        .hasMessageContaining("Email information is required");
  }
}
