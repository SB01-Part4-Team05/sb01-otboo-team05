package com.part4.team05.sb01otbooteam05.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "app.oauth2")
public class AppOAuth2Properties {
  private final Redirect redirect;

  @Getter
  @AllArgsConstructor
  public static class Redirect {
    private final String successUrl;
    private final String failureUrl;
  }
}
