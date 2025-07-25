package com.part4.team05.sb01otbooteam05.domain.user.service;

import com.part4.team05.sb01otbooteam05.domain.user.dto.ProfileDto;
import com.part4.team05.sb01otbooteam05.domain.user.dto.ProfileUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserDto;
import com.part4.team05.sb01otbooteam05.domain.user.entity.GenderType;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.entity.UserRole;
import com.part4.team05.sb01otbooteam05.domain.user.exception.EmailAlreadyExistsException;
import com.part4.team05.sb01otbooteam05.domain.user.exception.UserNotFoundException;
import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("사용자 서비스 핵심 테스트")
class UserServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private S3Service s3Service;
  @InjectMocks private UserServiceImpl userService;

  private User testUser;
  private final UUID TEST_USER_ID = UUID.randomUUID();
  private final String TEST_EMAIL = "test@example.com";

  @BeforeEach
  void setUp() {
    testUser = User.builder()
        .email(TEST_EMAIL)
        .name("Test User")
        .password("password")
        .role(UserRole.USER)
        .locked(false)
        .provider("LOCAL")
        .gender(GenderType.MALE)
        .birthDate(LocalDate.of(1990, 1, 1))
        .build();
    ReflectionTestUtils.setField(testUser, "id", TEST_USER_ID);
  }

  @Test
  @DisplayName("회원가입 성공")
  void signUp_Success() {
    UserCreateRequest request = new UserCreateRequest("Test User", TEST_EMAIL, "password123");
    when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
    when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    UserDto result = userService.signUp(request);

    assertThat(result.getId()).isEqualTo(TEST_USER_ID);
    assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("회원가입 실패 - 이메일 중복")
  void signUp_EmailExists() {
    UserCreateRequest request = new UserCreateRequest("Test User", TEST_EMAIL, "password123");
    when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

    assertThatThrownBy(() -> userService.signUp(request))
        .isInstanceOf(EmailAlreadyExistsException.class);
  }

  @Test
  @DisplayName("프로필 조회 성공")
  void getProfile_Success() {
    when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

    ProfileDto result = userService.getProfile(TEST_USER_ID);

    assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
    assertThat(result.getName()).isEqualTo("Test User");
  }

  @Test
  @DisplayName("프로필 업데이트 - 이름 변경")
  void updateProfile_UpdateName() {
    ProfileUpdateRequest request = new ProfileUpdateRequest("New Name", null, null, null, null);
    when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
    when(userRepository.save(testUser)).thenReturn(testUser);

    ProfileDto result = userService.updateProfile(TEST_USER_ID, request, null);

    assertThat(result).isNotNull();
    verify(userRepository).save(testUser);
  }

  @Test
  @DisplayName("프로필 이미지 업로드 성공")
  void updateProfile_UploadImage_Success() {
    MultipartFile image = mock(MultipartFile.class);
    when(image.isEmpty()).thenReturn(false);
    String expectedS3Url = "https://bucket.s3.amazonaws.com/profile/test.jpg";
    when(s3Service.uploadProfileImage(TEST_USER_ID, image)).thenReturn(expectedS3Url);

    ProfileUpdateRequest request = new ProfileUpdateRequest(null, null, null, null, null);
    when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
    when(userRepository.save(testUser)).thenReturn(testUser);

    ProfileDto result = userService.updateProfile(TEST_USER_ID, request, image);

    assertThat(result).isNotNull();
    verify(s3Service).uploadProfileImage(TEST_USER_ID, image);
    verify(userRepository).save(testUser);
  }

  @Test
  @DisplayName("사용자 없음 예외")
  void getUserEntityByIdOrThrow_UserNotFound() {
    when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.getUserEntityByIdOrThrow(TEST_USER_ID))
        .isInstanceOf(UserNotFoundException.class);
  }
}
