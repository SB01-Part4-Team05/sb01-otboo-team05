package com.part4.team05.sb01otbooteam05.domain.user.service;

import com.part4.team05.sb01otbooteam05.domain.user.dto.UserDto;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserLockUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserRoleUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.user.entity.UserRole;
import java.util.UUID;

public interface AdminService {

  // 사용자 목록 조회 (커서 기반 페이지네이션)
  UserDtoCursorResponse getUsers(
      String cursor,
      UUID idAfter,
      Integer limit,
      String sortBy,
      String sortDirection,
      String emailLike,
      UserRole roleEqual,
      Boolean locked
  );

  // 사용자 권한 변경
  UserDto updateUserRole(UUID userId, UserRoleUpdateRequest request);

  // 사용자 계정 잠금 상태 변경
  UUID updateUserLockStatus(UUID userId, UserLockUpdateRequest request);
}
