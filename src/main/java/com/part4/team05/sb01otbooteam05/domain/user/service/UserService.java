package com.part4.team05.sb01otbooteam05.domain.user.service;

import java.util.UUID;

import com.part4.team05.sb01otbooteam05.domain.user.dto.UserCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserDto;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;

public interface UserService {

  // 회원가입
  UserDto signUp(UserCreateRequest request);

  User getUserEntityByIdOrThrow(UUID userId);
}
