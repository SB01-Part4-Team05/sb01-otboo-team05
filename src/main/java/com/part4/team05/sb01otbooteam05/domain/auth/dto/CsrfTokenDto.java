package com.part4.team05.sb01otbooteam05.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CsrfTokenDto {
  private String headerName;      // 헤더 이름: X-XSRF-TOKEN
  private String token;           // 실제 토큰 값
  private String parameterName;   // 쿠키 이름: XSRF-TOKEN
}
