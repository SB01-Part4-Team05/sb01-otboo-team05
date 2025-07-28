package com.part4.team05.sb01otbooteam05.domain.auth.service;

import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService { //구글 로그인

  private final UserRepository userRepository;

  @Override
  public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
    log.info("CustomOidcUserService.loadUser 호출 ");

    try {
      OidcUser oidcUser = super.loadUser(userRequest);
      return processOidcUser(userRequest, oidcUser);

    } catch (OAuth2AuthenticationException e) {
      log.error("OIDC 인증 예외 발생: {}", e.getError().getDescription(), e);
      throw e;
    } catch (Exception e) {
      log.error("OIDC 사용자 로드 중 예상치 못한 오류 발생", e);
      throw new OAuth2AuthenticationException(
          new OAuth2Error("user_load_error", "OIDC user loading failed", null), e
      );
    }
  }

  @Transactional
  public OidcUser processOidcUser(OidcUserRequest userRequest, OidcUser oidcUser) {
    String provider = "GOOGLE"; // 구글만 OIDC 사용
    String providerId = oidcUser.getSubject();
    String email = oidcUser.getEmail();
    String name = oidcUser.getFullName();

    log.info("OIDC 추출된 정보 - providerId: {}, email: {}, name: {}", providerId, email, name);

    // 필수 정보 검증
    if (email == null || email.trim().isEmpty()) {
      log.error("이메일 정보가 없습니다.");
      throw new OAuth2AuthenticationException(
          new OAuth2Error("missing_email", "Email information is required", null)
      );
    }

    if (name == null || name.trim().isEmpty()) {
      log.warn("이름 정보가 없습니다. 이메일을 이름으로 사용합니다.");
      name = email.split("@")[0];
    }

    User user = findOrCreateUser(provider, providerId, email, name);

    log.info("OIDC 로그인 완료: userId={}, email={}", user.getId(), user.getEmail());

    return new CustomUserDetails(
        user.getId(),
        user.getEmail(),
        user.getRole().name(),
        oidcUser.getAttributes()
    );
  }

  private User findOrCreateUser(String provider, String providerId, String email, String name) {
    Optional<User> existingSocialUser = userRepository.findByProviderAndProviderId(provider, providerId);

    if (existingSocialUser.isPresent()) {
      User user = existingSocialUser.get();

      // 계정 잠금 상태 체크 추가
      if (user.isLocked()) {
        log.warn("잠금된 계정으로 소셜 로그인 시도: userId={}, email={}", user.getId(), user.getEmail());
        throw new OAuth2AuthenticationException(
            new OAuth2Error("account_locked", "Account is locked", null)
        );
      }

      log.info("기존 소셜 계정으로 로그인: userId={}, email={}", user.getId(), user.getEmail());
      return user;
    }

    Optional<User> existingLocalUser = userRepository.findByEmail(email);

    if (existingLocalUser.isPresent()) {
      User localUser = existingLocalUser.get();
      log.info("기존 계정 발견: userId={}, provider={}", localUser.getId(), localUser.getProvider());

      if ("LOCAL".equals(localUser.getProvider()) || localUser.getProvider() == null) {
        log.warn("이미 일반 회원가입된 이메일로 소셜 로그인 시도: email={}", email);
        throw new OAuth2AuthenticationException(
            new OAuth2Error("email_already_exists",
                "Email already registered with local account",
                null)
        );
      } else {
        log.warn("다른 소셜 서비스로 가입된 이메일: email={}, 기존provider={}, 시도provider={}",
            email, localUser.getProvider(), provider);
        throw new OAuth2AuthenticationException(
            new OAuth2Error("email_used_by_other_provider",
                String.format("Email already registered with %s",
                    localUser.getProvider() != null ? localUser.getProvider().toLowerCase() : "unknown"),
                null)
        );
      }
    }

    log.info("신규 OIDC 소셜 로그인 사용자 등록 시작: provider={}, email={}", provider, email);

    User newUser = User.createOAuthUser(name, email, provider, providerId);
    User savedUser = userRepository.save(newUser);

    log.info("새 사용자 DB 저장 완료: userId={}, email={}", savedUser.getId(), savedUser.getEmail());

    return savedUser;
  }
}
