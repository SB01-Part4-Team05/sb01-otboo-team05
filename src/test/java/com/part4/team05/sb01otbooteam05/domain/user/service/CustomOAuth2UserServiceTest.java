package com.part4.team05.sb01otbooteam05.domain.user.service;

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
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("카카오 OAuth2 서비스 핵심 테스트")
class CustomOAuth2UserServiceTest {

  @Mock private UserRepository userRepository;
  @InjectMocks private CustomOAuth2UserService customOAuth2UserService;

  @Test
  @DisplayName("카카오 OAuth2 기존 사용자 로그인 성공")
  void processOAuth2User_ExistingUser_Success() {
    String providerId = "123456789";
    String nickname = "카카오유저";
    String expectedEmail = "카카오유저@kakao.com";
    UUID testUserId = UUID.randomUUID();

    User testUser = User.builder()
        .email(expectedEmail)
        .name(nickname)
        .role(UserRole.USER)
        .locked(false)
        .provider("KAKAO")
        .providerId(providerId)
        .build();
    ReflectionTestUtils.setField(testUser, "id", testUserId);

    OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
    OAuth2User oAuth2User = mock(OAuth2User.class);
    ClientRegistration clientRegistration = mock(ClientRegistration.class);

    Map<String, Object> profile = new HashMap<>();
    profile.put("nickname", nickname);

    Map<String, Object> kakaoAccount = new HashMap<>();
    kakaoAccount.put("profile", profile);

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("id", providerId);
    attributes.put("kakao_account", kakaoAccount);

    when(oAuth2User.getAttributes()).thenReturn(attributes);
    when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
    when(clientRegistration.getRegistrationId()).thenReturn("kakao");
    when(userRepository.findByProviderAndProviderId("KAKAO", providerId))
        .thenReturn(Optional.of(testUser));

    OAuth2User result = customOAuth2UserService.processOAuth2User(userRequest, oAuth2User);

    assertThat(result).isInstanceOf(CustomUserDetails.class);
    CustomUserDetails customUserDetails = (CustomUserDetails) result;
    assertThat(customUserDetails.getUserId()).isEqualTo(testUserId);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("카카오 OAuth2 신규 사용자 등록 성공")
  void processOAuth2User_NewUser_Success() {
    String providerId = "123456789";
    String nickname = "카카오유저";
    String expectedEmail = "카카오유저@kakao.com";
    UUID testUserId = UUID.randomUUID();

    User testUser = User.builder()
        .email(expectedEmail)
        .name(nickname)
        .role(UserRole.USER)
        .locked(false)
        .provider("KAKAO")
        .providerId(providerId)
        .build();
    ReflectionTestUtils.setField(testUser, "id", testUserId);

    OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
    OAuth2User oAuth2User = mock(OAuth2User.class);
    ClientRegistration clientRegistration = mock(ClientRegistration.class);

    Map<String, Object> profile = new HashMap<>();
    profile.put("nickname", nickname);

    Map<String, Object> kakaoAccount = new HashMap<>();
    kakaoAccount.put("profile", profile);

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("id", providerId);
    attributes.put("kakao_account", kakaoAccount);

    when(oAuth2User.getAttributes()).thenReturn(attributes);
    when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
    when(clientRegistration.getRegistrationId()).thenReturn("kakao");
    when(userRepository.findByProviderAndProviderId("KAKAO", providerId))
        .thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    OAuth2User result = customOAuth2UserService.processOAuth2User(userRequest, oAuth2User);

    assertThat(result).isInstanceOf(CustomUserDetails.class);
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("카카오 OAuth2 계정 잠금 실패")
  void processOAuth2User_AccountLocked_ThrowsException() {
    String providerId = "123456789";
    String nickname = "카카오유저";
    String expectedEmail = "카카오유저@kakao.com";

    User lockedUser = User.builder()
        .email(expectedEmail)
        .name(nickname)
        .role(UserRole.USER)
        .locked(true)
        .provider("KAKAO")
        .providerId(providerId)
        .build();

    OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
    OAuth2User oAuth2User = mock(OAuth2User.class);
    ClientRegistration clientRegistration = mock(ClientRegistration.class);

    Map<String, Object> profile = new HashMap<>();
    profile.put("nickname", nickname);

    Map<String, Object> kakaoAccount = new HashMap<>();
    kakaoAccount.put("profile", profile);

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("id", providerId);
    attributes.put("kakao_account", kakaoAccount);

    when(oAuth2User.getAttributes()).thenReturn(attributes);
    when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
    when(clientRegistration.getRegistrationId()).thenReturn("kakao");
    when(userRepository.findByProviderAndProviderId("KAKAO", providerId))
        .thenReturn(Optional.of(lockedUser));

    assertThatThrownBy(() -> customOAuth2UserService.processOAuth2User(userRequest, oAuth2User))
        .isInstanceOf(OAuth2AuthenticationException.class)
        .hasMessageContaining("Account is locked");
  }

  @Test
  @DisplayName("지원하지 않는 프로바이더 실패")
  void processOAuth2User_UnsupportedProvider_ThrowsException() {
    OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
    OAuth2User oAuth2User = mock(OAuth2User.class);
    ClientRegistration clientRegistration = mock(ClientRegistration.class);

    when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
    when(clientRegistration.getRegistrationId()).thenReturn("google");

    assertThatThrownBy(() -> customOAuth2UserService.processOAuth2User(userRequest, oAuth2User))
        .isInstanceOf(OAuth2AuthenticationException.class)
        .hasMessageContaining("Only Kakao login is supported");
  }

  @Test
  @DisplayName("카카오 OAuth2 닉네임 없음 - 기본 이름 생성")
  void processOAuth2User_NoNickname_GenerateDefaultName() {
    String providerId = "123456789";

    OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
    OAuth2User oAuth2User = mock(OAuth2User.class);
    ClientRegistration clientRegistration = mock(ClientRegistration.class);

    Map<String, Object> profile = new HashMap<>();

    Map<String, Object> kakaoAccount = new HashMap<>();
    kakaoAccount.put("profile", profile);

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("id", providerId);
    attributes.put("kakao_account", kakaoAccount);

    when(oAuth2User.getAttributes()).thenReturn(attributes);
    when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
    when(clientRegistration.getRegistrationId()).thenReturn("kakao");
    when(userRepository.findByProviderAndProviderId("KAKAO", providerId))
        .thenReturn(Optional.empty());

    User savedUser = User.builder()
        .email("kakao1234@kakao.com")
        .name("카카오사용자1234")
        .role(UserRole.USER)
        .provider("KAKAO")
        .providerId(providerId)
        .build();
    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    OAuth2User result = customOAuth2UserService.processOAuth2User(userRequest, oAuth2User);

    assertThat(result).isInstanceOf(CustomUserDetails.class);
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("가상 이메일 생성 - 특수문자 제거")
  void generateKakaoVirtualEmail_SpecialCharacters() {
    String providerId = "123456789";
    String nickname = "테스트@#$유저";

    OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
    OAuth2User oAuth2User = mock(OAuth2User.class);
    ClientRegistration clientRegistration = mock(ClientRegistration.class);

    Map<String, Object> profile = new HashMap<>();
    profile.put("nickname", nickname);

    Map<String, Object> kakaoAccount = new HashMap<>();
    kakaoAccount.put("profile", profile);

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("id", providerId);
    attributes.put("kakao_account", kakaoAccount);

    when(oAuth2User.getAttributes()).thenReturn(attributes);
    when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
    when(clientRegistration.getRegistrationId()).thenReturn("kakao");
    when(userRepository.findByProviderAndProviderId("KAKAO", providerId))
        .thenReturn(Optional.empty());

    User savedUser = User.builder()
        .email("테스트유저@kakao.com")
        .name(nickname)
        .role(UserRole.USER)
        .provider("KAKAO")
        .providerId(providerId)
        .build();
    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    OAuth2User result = customOAuth2UserService.processOAuth2User(userRequest, oAuth2User);

    assertThat(result).isInstanceOf(CustomUserDetails.class);
  }
}
