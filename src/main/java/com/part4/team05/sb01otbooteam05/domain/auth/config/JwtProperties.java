package com.part4.team05.sb01otbooteam05.domain.auth.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
  private final String secret;
  private final Long accessTokenExpiration;
  private final Long refreshTokenExpiration;

  public JwtProperties(String secret, Long accessTokenExpiration, Long refreshTokenExpiration) {
    this.secret = secret;
    this.accessTokenExpiration = accessTokenExpiration != null ? accessTokenExpiration : 1800000L;
    this.refreshTokenExpiration = refreshTokenExpiration != null ? refreshTokenExpiration : 604800000L;
  }
}
