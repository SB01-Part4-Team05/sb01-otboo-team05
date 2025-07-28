package com.part4.team05.sb01otbooteam05.domain.user.controller;

import com.part4.team05.sb01otbooteam05.domain.user.dto.UserDto;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserLockUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserRoleUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.user.entity.UserRole;
import com.part4.team05.sb01otbooteam05.domain.user.service.AdminService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class AdminController implements AdminControllerDoc{

  private final AdminService adminService;

  /**
   * 계정 목록 조회
   */
  @GetMapping
  public ResponseEntity<UserDtoCursorResponse> getUsers(
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam(required = false) Integer limit,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false) String sortDirection,
      @RequestParam(required = false) String emailLike,
      @RequestParam(required = false) UserRole roleEqual,
      @RequestParam(required = false) Boolean locked) {

    UserDtoCursorResponse response = adminService.getUsers(
        cursor, idAfter, limit, sortBy, sortDirection,
        emailLike, roleEqual, locked
    );

    return ResponseEntity.ok(response);
  }

  /**
   * 권한 수정
   */
  @PatchMapping("/{userId}/role")
  public ResponseEntity<UserDto> updateUserRole(
      @PathVariable UUID userId,
      @Valid @RequestBody UserRoleUpdateRequest request) {

    UserDto updatedUser = adminService.updateUserRole(userId, request);
    return ResponseEntity.ok(updatedUser);
  }

  /**
   * 계정 잠금 상태 변경
   */
  @PatchMapping("/{userId}/lock")
  public ResponseEntity<UUID> updateUserLockStatus(
      @PathVariable UUID userId,
      @Valid @RequestBody UserLockUpdateRequest request) {

    UUID updatedUserId = adminService.updateUserLockStatus(userId, request);
    return ResponseEntity.ok(updatedUserId);
  }
}
