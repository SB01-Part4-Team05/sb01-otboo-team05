package com.part4.team05.sb01otbooteam05.domain.user.service;

import com.part4.team05.sb01otbooteam05.domain.auth.repository.RefreshTokenRepository;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserDto;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserLockUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserRoleUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.entity.UserRole;
import com.part4.team05.sb01otbooteam05.domain.user.exception.UserNotFoundException;
import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("관리자 서비스 핵심 테스트")
class AdminServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private RefreshTokenRepository refreshTokenRepository;
  @InjectMocks private AdminServiceImpl adminService;

  private User testUser;
  private final UUID TEST_USER_ID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    testUser = User.builder()
        .email("test@example.com")
        .name("Test User")
        .password("password")
        .role(UserRole.USER)
        .locked(false)
        .provider("LOCAL")
        .build();
    ReflectionTestUtils.setField(testUser, "id", TEST_USER_ID);
    ReflectionTestUtils.setField(testUser, "createdAt", LocalDateTime.now());
  }

  @Test
  @DisplayName("사용자 목록 조회 성공")
  void getUsers_Success() {
    Page<User> userPage = new PageImpl<>(List.of(testUser));
    when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
    when(userRepository.count(any(Specification.class))).thenReturn(1L);

    UserDtoCursorResponse response = adminService.getUsers(null, null, null, null, null, null, null, null);

    assertThat(response.getData()).hasSize(1);
    assertThat(response.getTotalCount()).isEqualTo(1L);
    verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
  }

  @Test
  @DisplayName("사용자 권한 변경 성공")
  void updateUserRole_Success() {
    UserRoleUpdateRequest request = new UserRoleUpdateRequest(UserRole.ADMIN);
    when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
    when(userRepository.save(testUser)).thenReturn(testUser);

    UserDto result = adminService.updateUserRole(TEST_USER_ID, request);

    assertThat(result.getId()).isEqualTo(TEST_USER_ID);
    verify(userRepository).save(testUser);
    verify(refreshTokenRepository).revokeAllByUserId(TEST_USER_ID);
  }

  @Test
  @DisplayName("사용자 권한 변경 실패 - 사용자 없음")
  void updateUserRole_UserNotFound() {
    UserRoleUpdateRequest request = new UserRoleUpdateRequest(UserRole.ADMIN);
    when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> adminService.updateUserRole(TEST_USER_ID, request))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  @DisplayName("계정 잠금 성공")
  void updateUserLockStatus_Lock_Success() {
    UserLockUpdateRequest request = new UserLockUpdateRequest(true);
    when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

    UUID result = adminService.updateUserLockStatus(TEST_USER_ID, request);

    assertThat(result).isEqualTo(TEST_USER_ID);
    verify(userRepository).save(testUser);
    verify(refreshTokenRepository).revokeAllByUserId(TEST_USER_ID);
  }

  @Test
  @DisplayName("계정 잠금 해제 성공")
  void updateUserLockStatus_Unlock_Success() {
    UserLockUpdateRequest request = new UserLockUpdateRequest(false);
    when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

    UUID result = adminService.updateUserLockStatus(TEST_USER_ID, request);

    assertThat(result).isEqualTo(TEST_USER_ID);
    verify(userRepository).save(testUser);
    verify(refreshTokenRepository, never()).revokeAllByUserId(any());
  }
}
