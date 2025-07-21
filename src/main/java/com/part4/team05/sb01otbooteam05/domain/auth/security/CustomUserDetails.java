package com.part4.team05.sb01otbooteam05.domain.auth.security;

import java.util.Map;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Getter
public class CustomUserDetails implements UserDetails, OAuth2User, OidcUser {

  private final UUID userId;
  private final String email;
  private final String role;
  private Map<String, Object> attributes;

  // 기존 JWT 인증을 위한 생성자
  public CustomUserDetails(UUID userId, String email, String role) {
    this.userId = userId;
    this.email = email;
    this.role = role;
  }

  // OAuth2 인증을 위한 생성자
  public CustomUserDetails(UUID userId, String email, String role, Map<String, Object> attributes) {
    this.userId = userId;
    this.email = email;
    this.role = role;
    this.attributes = attributes;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public String getName() {
    return email;
  }

  @Override
  public Map<String, Object> getClaims() {
    return attributes != null ? attributes : Collections.emptyMap();
  }

  @Override
  public OidcUserInfo getUserInfo() {
    return null;
  }

  @Override
  public OidcIdToken getIdToken() {
    return null;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
