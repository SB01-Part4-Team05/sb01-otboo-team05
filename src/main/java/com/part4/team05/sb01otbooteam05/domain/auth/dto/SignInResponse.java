package com.part4.team05.sb01otbooteam05.domain.auth.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignInResponse {
  private String accessToken;
  private String refreshToken;
  private UUID userId;
  private String email;
  private String name;
  private String role;
}
