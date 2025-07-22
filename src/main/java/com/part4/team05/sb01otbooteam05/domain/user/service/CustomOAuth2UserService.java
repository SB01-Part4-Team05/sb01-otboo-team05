package com.part4.team05.sb01otbooteam05.domain.user.service;

import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService { //카카오 로그인

  private final UserRepository userRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    log.info("CustomOAuth2UserService.loadUser 호출 ");

    try {
      OAuth2User oAuth2User = super.loadUser(userRequest);
      return processOAuth2User(userRequest, oAuth2User);

    } catch (OAuth2AuthenticationException e) {
      log.error("OAuth2 인증 예외 발생: {}", e.getError().getDescription(), e);
      throw e;
    } catch (Exception e) {
      log.error("OAuth2 사용자 로드 중 예상치 못한 오류 발생", e);
      throw new OAuth2AuthenticationException(
          new OAuth2Error("user_load_error", "User loading failed", null), e
      );
    }
  }

  @Transactional
  public OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
    String provider = userRequest.getClientRegistration().getRegistrationId().toUpperCase();

    if (!"KAKAO".equals(provider)) {
      log.warn("CustomOAuth2UserService는 KAKAO 로그인만 지원합니다. 현재 provider: {}", provider);
      throw new OAuth2AuthenticationException(new OAuth2Error("unsupported_provider",
          "Only Kakao login is supported by this service.", null));
    }

    Map<String, Object> attributes = oAuth2User.getAttributes();
    log.info("Provider: {}, OAuth2 Attributes: {}", provider, attributes);

    String providerId = String.valueOf(attributes.get("id"));

    Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
    Map<String, Object> profile = kakaoAccount != null ?
        (Map<String, Object>) kakaoAccount.get("profile") : null;
    String name = profile != null ? (String) profile.get("nickname") : null;

    // Kakao 가상 이메일 생성 -> 카카오는 이메일을 제공하지 않아서 요구사항에 따라서.
    String email = generateKakaoVirtualEmail(name, providerId);
    log.info("Kakao 추출된 정보 - providerId: {}, 가상email: {}, name: {}", providerId, email, name);

    if (name == null || name.trim().isEmpty()) {
      name = "카카오사용자" + providerId.substring(0, Math.min(4, providerId.length()));
      log.warn("이름 정보가 없어서 생성된 이름 사용: {}", name);
    }

    // 사용자 조회/생성
    User user = findOrCreateUser(provider, providerId, email, name);

    log.info("카카오 로그인 완료: userId={}, email={}", user.getId(), user.getEmail());

    return new CustomUserDetails(
        user.getId(),
        user.getEmail(),
        user.getRole().name(),
        oAuth2User.getAttributes()
    );
  }

  private User findOrCreateUser(String provider, String providerId, String email, String name) {
    Optional<User> existingSocialUser = userRepository.findByProviderAndProviderId(provider, providerId);
    if (existingSocialUser.isPresent()) {
      User user = existingSocialUser.get();
      log.info("기존 소셜 계정으로 로그인: userId={}, email={}", user.getId(), user.getEmail());
      return user;
    }

    log.info("신규 카카오 로그인 사용자 등록 시작: email={}", email);
    User newUser = User.createOAuthUser(name, email, provider, providerId);
    User savedUser = userRepository.save(newUser);
    log.info("새 사용자 DB 저장 완료: userId={}, email={}", savedUser.getId(), savedUser.getEmail());

    return savedUser;
  }

  /**
   * Kakao 가상 이메일 생성
   * 닉네임@kakao.com
   */
  private String generateKakaoVirtualEmail(String nickname, String providerId) {
    if (nickname != null && !nickname.trim().isEmpty()) {
      String cleanNickname = nickname.replaceAll("[^a-zA-Z0-9가-힣]", "");
      if (!cleanNickname.isEmpty()) {
        return cleanNickname + "@kakao.com";
      }
    }
    return "kakao" + providerId + "@kakao.com";
  }
}
