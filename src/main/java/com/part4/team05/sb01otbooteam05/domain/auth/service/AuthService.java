package com.part4.team05.sb01otbooteam05.domain.auth.service;

import com.part4.team05.sb01otbooteam05.domain.auth.dto.SignInRequest;
import com.part4.team05.sb01otbooteam05.domain.auth.dto.SignInResponse;
import com.part4.team05.sb01otbooteam05.domain.auth.dto.TokenRefreshRequest;

import java.util.UUID;

public interface AuthService {

  // 로그인
  SignInResponse signIn(SignInRequest request);

  // 로그아웃
  void signOut(UUID userId);

  // 토큰 재발급 (리프레시 토큰 + 액세스 토큰 모두)
  SignInResponse refreshToken(TokenRefreshRequest request);

  // 액세스 토큰만 조회 (리프레시 토큰 유지)
  String getAccessTokenOnly(TokenRefreshRequest request);

  //비밀번호 초기화
  void resetPassword(String email);

}
