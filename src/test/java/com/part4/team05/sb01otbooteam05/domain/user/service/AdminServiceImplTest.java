package com.part4.team05.sb01otbooteam05.domain.user.service;

import com.part4.team05.sb01otbooteam05.domain.auth.repository.RefreshTokenRepository;
import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.notification.service.NotificationService;
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
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("관리자 서비스 보안 강화 테스트")
class AdminServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private RefreshTokenRepository refreshTokenRepository;
  @Mock private SecurityContext securityContext;
  @Mock private Authentication authentication;
  @Mock private CustomUserDetails userDetails;
  @Mock private NotificationService notificationService;
  @InjectMocks private AdminServiceImpl adminService;

  private User testUser;
  private User superAdmin;
  private User normalAdmin;
  private final UUID TEST_USER_ID = UUID.randomUUID();
  private final UUID SUPER_ADMIN_ID = UUID.randomUUID();
  private final UUID NORMAL_ADMIN_ID = UUID.randomUUID();
  private final UUID CURRENT_ADMIN_ID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    // 일반 사용자
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

    // 슈퍼 어드민 (@otboo.com 이메일)
    superAdmin = User.builder()
        .email("admin@otboo.com")
        .name("Super Admin")
        .password("password")
        .role(UserRole.ADMIN)
        .locked(false)
        .provider("LOCAL")
        .build();
    ReflectionTestUtils.setField(superAdmin, "id", SUPER_ADMIN_ID);

    // 일반 어드민
    normalAdmin = User.builder()
        .email("normal@admin.com")
        .name("Normal Admin")
        .password("password")
        .role(UserRole.ADMIN)
        .locked(false)
        .provider("LOCAL")
        .build();
    ReflectionTestUtils.setField(normalAdmin, "id", NORMAL_ADMIN_ID);
  }

  // Security Context 모킹 헬퍼 메서드
  private void setupSecurityContext() {
    when(userDetails.getUserId()).thenReturn(CURRENT_ADMIN_ID);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(securityContext.getAuthentication()).thenReturn(authentication);
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
    try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
      setupSecurityContext();
      mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      UserRoleUpdateRequest request = new UserRoleUpdateRequest(UserRole.ADMIN);
      when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
      when(userRepository.save(testUser)).thenReturn(testUser);

      UserDto result = adminService.updateUserRole(TEST_USER_ID, request);

      assertThat(result.getId()).isEqualTo(TEST_USER_ID);
      verify(userRepository).save(testUser);
      verify(refreshTokenRepository).revokeAllByUserId(TEST_USER_ID);
    }
  }

  @Test
  @DisplayName("사용자 권한 변경 실패 - 자기 자신 수정 시도")
  void updateUserRole_SelfModification_ThrowsException() {
    try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
      setupSecurityContext();
      mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      UserRoleUpdateRequest request = new UserRoleUpdateRequest(UserRole.USER);
      when(userRepository.findById(CURRENT_ADMIN_ID)).thenReturn(Optional.of(normalAdmin));

      assertThatThrownBy(() -> adminService.updateUserRole(CURRENT_ADMIN_ID, request))
          .isInstanceOf(SecurityException.class)
          .hasMessage("자기 자신의 권한은 변경할 수 없습니다.");

      verify(userRepository, never()).save(any());
      verify(refreshTokenRepository, never()).revokeAllByUserId(any());
    }
  }

  @Test
  @DisplayName("사용자 권한 변경 실패 - 슈퍼 어드민 수정 시도")
  void updateUserRole_SuperAdminModification_ThrowsException() {
    try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
      setupSecurityContext();
      mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      UserRoleUpdateRequest request = new UserRoleUpdateRequest(UserRole.USER);
      when(userRepository.findById(SUPER_ADMIN_ID)).thenReturn(Optional.of(superAdmin));

      assertThatThrownBy(() -> adminService.updateUserRole(SUPER_ADMIN_ID, request))
          .isInstanceOf(SecurityException.class)
          .hasMessage("슈퍼 어드민의 권한은 변경할 수 없습니다.");

      verify(userRepository, never()).save(any());
      verify(refreshTokenRepository, never()).revokeAllByUserId(any());
    }
  }

  @Test
  @DisplayName("사용자 권한 변경 실패 - 마지막 관리자 권한 제거 시도")
  void updateUserRole_LastAdminRemoval_ThrowsException() {
    try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
      setupSecurityContext();
      mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      UserRoleUpdateRequest request = new UserRoleUpdateRequest(UserRole.USER);
      when(userRepository.findById(NORMAL_ADMIN_ID)).thenReturn(Optional.of(normalAdmin));
      when(userRepository.countActiveAdmins()).thenReturn(1L);

      assertThatThrownBy(() -> adminService.updateUserRole(NORMAL_ADMIN_ID, request))
          .isInstanceOf(SecurityException.class)
          .hasMessage("시스템에 최소 1명의 관리자가 있어야 합니다.");

      verify(userRepository, never()).save(any());
      verify(refreshTokenRepository, never()).revokeAllByUserId(any());
    }
  }

  @Test
  @DisplayName("사용자 권한 변경 실패 - 사용자 없음")
  void updateUserRole_UserNotFound() {
    try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
      setupSecurityContext();
      mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      UserRoleUpdateRequest request = new UserRoleUpdateRequest(UserRole.ADMIN);
      when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> adminService.updateUserRole(TEST_USER_ID, request))
          .isInstanceOf(UserNotFoundException.class);
    }
  }

  @Test
  @DisplayName("계정 잠금 성공")
  void updateUserLockStatus_Lock_Success() {
    try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
      setupSecurityContext();
      mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      UserLockUpdateRequest request = new UserLockUpdateRequest(true);
      when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

      UUID result = adminService.updateUserLockStatus(TEST_USER_ID, request);

      assertThat(result).isEqualTo(TEST_USER_ID);
      verify(userRepository).save(testUser);
      verify(refreshTokenRepository).revokeAllByUserId(TEST_USER_ID);
    }
  }

  @Test
  @DisplayName("계정 잠금 실패 - 자기 자신 잠금 시도")
  void updateUserLockStatus_SelfLock_ThrowsException() {
    try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
      setupSecurityContext();
      mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      UserLockUpdateRequest request = new UserLockUpdateRequest(true);
      when(userRepository.findById(CURRENT_ADMIN_ID)).thenReturn(Optional.of(normalAdmin));

      assertThatThrownBy(() -> adminService.updateUserLockStatus(CURRENT_ADMIN_ID, request))
          .isInstanceOf(SecurityException.class)
          .hasMessage("자기 자신의 계정은 잠글 수 없습니다.");

      verify(userRepository, never()).save(any());
      verify(refreshTokenRepository, never()).revokeAllByUserId(any());
    }
  }

  @Test
  @DisplayName("계정 잠금 실패 - 슈퍼 어드민 잠금 시도")
  void updateUserLockStatus_SuperAdminLock_ThrowsException() {
    try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
      setupSecurityContext();
      mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      UserLockUpdateRequest request = new UserLockUpdateRequest(true);
      when(userRepository.findById(SUPER_ADMIN_ID)).thenReturn(Optional.of(superAdmin));

      assertThatThrownBy(() -> adminService.updateUserLockStatus(SUPER_ADMIN_ID, request))
          .isInstanceOf(SecurityException.class)
          .hasMessage("슈퍼 어드민 계정은 잠글 수 없습니다.");

      verify(userRepository, never()).save(any());
      verify(refreshTokenRepository, never()).revokeAllByUserId(any());
    }
  }

  @Test
  @DisplayName("계정 잠금 실패 - 마지막 활성 관리자 잠금 시도")
  void updateUserLockStatus_LastActiveAdminLock_ThrowsException() {
    try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
      setupSecurityContext();
      mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      UserLockUpdateRequest request = new UserLockUpdateRequest(true);
      when(userRepository.findById(NORMAL_ADMIN_ID)).thenReturn(Optional.of(normalAdmin));
      when(userRepository.countActiveAdmins()).thenReturn(1L);

      assertThatThrownBy(() -> adminService.updateUserLockStatus(NORMAL_ADMIN_ID, request))
          .isInstanceOf(SecurityException.class)
          .hasMessage("시스템에 최소 1명의 활성 관리자가 있어야 합니다.");

      verify(userRepository, never()).save(any());
      verify(refreshTokenRepository, never()).revokeAllByUserId(any());
    }
  }

  @Test
  @DisplayName("계정 잠금 해제 성공")
  void updateUserLockStatus_Unlock_Success() {
    try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
      setupSecurityContext();
      mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      UserLockUpdateRequest request = new UserLockUpdateRequest(false);
      when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

      UUID result = adminService.updateUserLockStatus(TEST_USER_ID, request);

      assertThat(result).isEqualTo(TEST_USER_ID);
      verify(userRepository).save(testUser);
      verify(refreshTokenRepository, never()).revokeAllByUserId(any());
    }
  }

  @Test
  @DisplayName("인증되지 않은 요청 - SecurityException 발생")
  void updateUserRole_Unauthenticated_ThrowsSecurityException() {
    try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
      mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
      when(securityContext.getAuthentication()).thenReturn(null);

      UserRoleUpdateRequest request = new UserRoleUpdateRequest(UserRole.ADMIN);

      assertThatThrownBy(() -> adminService.updateUserRole(TEST_USER_ID, request))
          .isInstanceOf(SecurityException.class)
          .hasMessage("인증되지 않은 요청입니다.");
    }
  }

  @Test
  @DisplayName("사용자 목록 조회 - 이메일 검색")
  void getUsers_WithEmailFilter() {
    Page<User> userPage = new PageImpl<>(List.of(testUser));
    when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
    when(userRepository.count(any(Specification.class))).thenReturn(1L);

    UserDtoCursorResponse response = adminService.getUsers(
        null, null, null, null, null, "test", null, null);

    assertThat(response.getData()).hasSize(1);
    verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
  }

  @Test
  @DisplayName("사용자 목록 조회 - 권한 필터")
  void getUsers_WithRoleFilter() {
    Page<User> userPage = new PageImpl<>(List.of(testUser));
    when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
    when(userRepository.count(any(Specification.class))).thenReturn(1L);

    UserDtoCursorResponse response = adminService.getUsers(
        null, null, null, null, null, null, UserRole.USER, null);

    assertThat(response.getData()).hasSize(1);
    verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
  }

  @Test
  @DisplayName("사용자 목록 조회 - 잠금 상태 필터")
  void getUsers_WithLockedFilter() {
    Page<User> userPage = new PageImpl<>(List.of(testUser));
    when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
    when(userRepository.count(any(Specification.class))).thenReturn(1L);

    UserDtoCursorResponse response = adminService.getUsers(
        null, null, null, null, null, null, null, false);

    assertThat(response.getData()).hasSize(1);
    verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
  }

  @Test
  @DisplayName("사용자 목록 조회 - 커서 기반 페이지네이션")
  void getUsers_WithCursor() {
    UUID cursorId = UUID.randomUUID();
    Page<User> userPage = new PageImpl<>(List.of(testUser));
    when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
    when(userRepository.count(any(Specification.class))).thenReturn(1L);

    UserDtoCursorResponse response = adminService.getUsers(
        "cursor", cursorId, 10, "createdAt", "ASCENDING", null, null, null);

    assertThat(response.getData()).hasSize(1);
    assertThat(response.getSortBy()).isEqualTo("createdAt");
    assertThat(response.getSortDirection()).isEqualTo("ASCENDING");
  }

  @Test
  @DisplayName("사용자 목록 조회 - 다음 페이지 존재")
  void getUsers_HasNextPage() {
    List<User> users = List.of(testUser, testUser);
    Page<User> userPage = new PageImpl<>(users);
    when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
    when(userRepository.count(any(Specification.class))).thenReturn(2L);

    UserDtoCursorResponse response = adminService.getUsers(
        null, null, 1, null, null, null, null, null);

    assertThat(response.isHasNext()).isTrue();
    assertThat(response.getNextCursor()).isNotNull();
  }

  @Test
  @DisplayName("사용자 목록 조회 - 기본 파라미터 동작 확인")
  void getUsers_DefaultParameters_WorksCorrectly() {
    Page<User> userPage = new PageImpl<>(List.of(testUser));
    when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
    when(userRepository.count(any(Specification.class))).thenReturn(1L);

    UserDtoCursorResponse response = adminService.getUsers(null, null, null, null, null, null, null, null);

    assertThat(response.getData()).hasSize(1);
    assertThat(response.getTotalCount()).isEqualTo(1L);
    assertThat(response.getSortBy()).isNotNull();
    assertThat(response.getSortDirection()).isNotNull();
    verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
  }

  @Test
  @DisplayName("사용자 목록 조회 - ASCENDING 정렬")
  void getUsers_AscendingSort() {
    Page<User> userPage = new PageImpl<>(List.of(testUser));
    when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
    when(userRepository.count(any(Specification.class))).thenReturn(1L);

    UserDtoCursorResponse response = adminService.getUsers(
        null, null, null, "name", "ASCENDING", null, null, null);

    assertThat(response.getData()).hasSize(1);
    assertThat(response.getSortBy()).isEqualTo("name");
    assertThat(response.getSortDirection()).isEqualTo("ASCENDING");
  }
}
