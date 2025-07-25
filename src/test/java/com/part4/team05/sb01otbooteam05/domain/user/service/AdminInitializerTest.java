package com.part4.team05.sb01otbooteam05.domain.user.service;

import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.entity.UserRole;
import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("관리자 초기화 서비스 핵심 테스트")
class AdminInitializerTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @InjectMocks private AdminInitializer adminInitializer;

  private final String TEST_ADMIN_EMAIL = "admintest@test.com";
  private final String TEST_ADMIN_PASSWORD = "admintest123";
  private final String TEST_ADMIN_NAME = "관리자";
  private final String ENCODED_PASSWORD = "encodedAdminPassword";

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(adminInitializer, "adminEmail", TEST_ADMIN_EMAIL);
    ReflectionTestUtils.setField(adminInitializer, "adminPassword", TEST_ADMIN_PASSWORD);
    ReflectionTestUtils.setField(adminInitializer, "adminName", TEST_ADMIN_NAME);
  }

  @Test
  @DisplayName("관리자 계정 생성 성공")
  void run_CreateNewAdminAccount_Success() throws Exception {
    when(userRepository.existsByEmail(TEST_ADMIN_EMAIL)).thenReturn(false);
    when(passwordEncoder.encode(TEST_ADMIN_PASSWORD)).thenReturn(ENCODED_PASSWORD);

    User mockSavedUser = mock(User.class);
    when(userRepository.save(any(User.class))).thenReturn(mockSavedUser);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

    adminInitializer.run();

    verify(userRepository).existsByEmail(TEST_ADMIN_EMAIL);
    verify(passwordEncoder).encode(TEST_ADMIN_PASSWORD);
    verify(userRepository).save(userCaptor.capture());

    User savedUser = userCaptor.getValue();
    assertThat(savedUser.getEmail()).isEqualTo(TEST_ADMIN_EMAIL);
    assertThat(savedUser.getName()).isEqualTo(TEST_ADMIN_NAME);
    assertThat(savedUser.getPassword()).isEqualTo(ENCODED_PASSWORD);
    assertThat(savedUser.getRole()).isEqualTo(UserRole.ADMIN);
    assertThat(savedUser.isLocked()).isFalse();
  }

  @Test
  @DisplayName("관리자 계정 생성 생략 - 기존 계정 존재")
  void run_AdminAccountAlreadyExists_Skip() throws Exception {
    when(userRepository.existsByEmail(TEST_ADMIN_EMAIL)).thenReturn(true);

    adminInitializer.run();

    verify(userRepository).existsByEmail(TEST_ADMIN_EMAIL);
    verify(passwordEncoder, never()).encode(anyString());
    verify(userRepository, never()).save(any(User.class));
  }
}
