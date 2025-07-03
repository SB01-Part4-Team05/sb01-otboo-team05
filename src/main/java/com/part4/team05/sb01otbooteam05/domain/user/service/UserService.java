package com.part4.team05.sb01otbooteam05.domain.user.service;

import java.util.UUID;

import com.part4.team05.sb01otbooteam05.domain.user.dto.UserCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserDto;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.dto.ProfileDto;
import com.part4.team05.sb01otbooteam05.domain.user.dto.ProfileUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

  // 회원가입
  UserDto signUp(UserCreateRequest request);

  User getUserEntityByIdOrThrow(UUID userId);

  // 프로필 조회
  ProfileDto getProfile(UUID userId);

  // 프로필 업데이트
  ProfileDto updateProfile(UUID userId, ProfileUpdateRequest request, MultipartFile image);
}
