package com.part4.team05.sb01otbooteam05.domain.user.service;

import java.util.List;
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

  //비밀번호 변경
  void changePassword(UUID userId, String newPassword);

  //특정 x,y 값(위치)를 가진 유저 Id 리스트 조회
  List<UUID> findUserIdsByLocation(int x, int y);
}
