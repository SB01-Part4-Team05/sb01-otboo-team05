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
import java.util.List;
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

  @Test
  @DisplayName("비밀번호 변경 성공")
  void changePassword_Success() {
    String newPassword = "newPassword123";
    when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
    when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
    when(userRepository.save(testUser)).thenReturn(testUser);

    userService.changePassword(TEST_USER_ID, newPassword);

    verify(passwordEncoder).encode(newPassword);
    verify(userRepository).save(testUser);
  }

  @Test
  @DisplayName("위치별 사용자 ID 조회 성공")
  void findUserIdsByLocation_Success() {
    int x = 60, y = 127;
    List<User> users = List.of(testUser);
    when(userRepository.findByLocationXAndLocationY(x, y)).thenReturn(users);

    List<UUID> result = userService.findUserIdsByLocation(x, y);

    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo(TEST_USER_ID);
  }

  @Test
  @DisplayName("프로필 업데이트 - 성별 변경")
  void updateProfile_UpdateGender() {
    ProfileUpdateRequest request = new ProfileUpdateRequest(null, "FEMALE", null, null, null);
    when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
    when(userRepository.save(testUser)).thenReturn(testUser);

    ProfileDto result = userService.updateProfile(TEST_USER_ID, request, null);

    assertThat(result).isNotNull();
    verify(userRepository).save(testUser);
  }

  @Test
  @DisplayName("프로필 업데이트 - 잘못된 성별 값")
  void updateProfile_InvalidGender() {
    ProfileUpdateRequest request = new ProfileUpdateRequest(null, "INVALID", null, null, null);
    when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

    assertThatThrownBy(() -> userService.updateProfile(TEST_USER_ID, request, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("잘못된 성별 값입니다");
  }

  @Test
  @DisplayName("프로필 업데이트 - 생년월일 변경")
  void updateProfile_UpdateBirthDate() {
    LocalDate newBirthDate = LocalDate.of(1995, 5, 15);
    ProfileUpdateRequest request = new ProfileUpdateRequest(null, null, newBirthDate, null, null);
    when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
    when(userRepository.save(testUser)).thenReturn(testUser);

    ProfileDto result = userService.updateProfile(TEST_USER_ID, request, null);

    assertThat(result).isNotNull();
    verify(userRepository).save(testUser);
  }

  @Test
  @DisplayName("프로필 업데이트 - 위치 정보 변경")
  void updateProfile_UpdateLocation() {
    ProfileUpdateRequest.LocationRequest locationRequest =
        new ProfileUpdateRequest.LocationRequest(37.5665, 126.9780, 60, 127, List.of("서울", "중구"));
    ProfileUpdateRequest request = new ProfileUpdateRequest(null, null, null, locationRequest, null);
    when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
    when(userRepository.save(testUser)).thenReturn(testUser);

    ProfileDto result = userService.updateProfile(TEST_USER_ID, request, null);

    assertThat(result).isNotNull();
    verify(userRepository).save(testUser);
  }

  @Test
  @DisplayName("프로필 업데이트 - 온도 민감도 변경")
  void updateProfile_UpdateTemperatureSensitivity() {
    ProfileUpdateRequest request = new ProfileUpdateRequest(null, null, null, null, 3);
    when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
    when(userRepository.save(testUser)).thenReturn(testUser);

    ProfileDto result = userService.updateProfile(TEST_USER_ID, request, null);

    assertThat(result).isNotNull();
    verify(userRepository).save(testUser);
  }

  @Test
  @DisplayName("프로필 이미지 업데이트 - 기존 이미지 삭제")
  void updateProfile_ReplaceExistingImage() {
    MultipartFile image = mock(MultipartFile.class);
    when(image.isEmpty()).thenReturn(false);

    testUser.updateProfileImageUrl("https://bucket.s3.amazonaws.com/profile/old.jpg");

    String newImageUrl = "https://bucket.s3.amazonaws.com/profile/new.jpg";
    when(s3Service.uploadProfileImage(TEST_USER_ID, image)).thenReturn(newImageUrl);
    when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
    when(userRepository.save(testUser)).thenReturn(testUser);

    ProfileUpdateRequest request = new ProfileUpdateRequest(null, null, null, null, null);
    ProfileDto result = userService.updateProfile(TEST_USER_ID, request, image);

    assertThat(result).isNotNull();
    verify(s3Service).deleteFile("https://bucket.s3.amazonaws.com/profile/old.jpg");
    verify(s3Service).uploadProfileImage(TEST_USER_ID, image);
  }

  @Test
  @DisplayName("프로필 이미지 업데이트 - 빈 이미지 파일")
  void updateProfile_EmptyImageFile() {
    MultipartFile image = mock(MultipartFile.class);
    when(image.isEmpty()).thenReturn(true);

    ProfileUpdateRequest request = new ProfileUpdateRequest(null, null, null, null, null);
    when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
    when(userRepository.save(testUser)).thenReturn(testUser);

    ProfileDto result = userService.updateProfile(TEST_USER_ID, request, image);

    assertThat(result).isNotNull();
    verify(s3Service, never()).uploadProfileImage(any(), any());
    verify(userRepository).save(testUser);
  }

  @Test
  @DisplayName("프로필 업데이트 - 모든 필드 한 번에 변경")
  void updateProfile_UpdateAllFields() {
    ProfileUpdateRequest.LocationRequest locationRequest =
        new ProfileUpdateRequest.LocationRequest(37.5665, 126.9780, 60, 127, List.of("서울", "중구"));
    ProfileUpdateRequest request = new ProfileUpdateRequest(
        "새로운 이름",
        "FEMALE",
        LocalDate.of(1995, 5, 15),
        locationRequest,
        4
    );

    when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
    when(userRepository.save(testUser)).thenReturn(testUser);

    ProfileDto result = userService.updateProfile(TEST_USER_ID, request, null);

    assertThat(result).isNotNull();
    verify(userRepository).save(testUser);
  }


  @Test
  @DisplayName("프로필 이미지 업데이트 - 기존 이미지 없고 S3 아닌 URL")
  void updateProfile_ReplaceExistingImageNotS3() {
    MultipartFile image = mock(MultipartFile.class);
    when(image.isEmpty()).thenReturn(false);

    testUser.updateProfileImageUrl("/uploads/profile/local.jpg");

    String newImageUrl = "https://bucket.s3.amazonaws.com/profile/new.jpg";
    when(s3Service.uploadProfileImage(TEST_USER_ID, image)).thenReturn(newImageUrl);
    when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
    when(userRepository.save(testUser)).thenReturn(testUser);

    ProfileUpdateRequest request = new ProfileUpdateRequest(null, null, null, null, null);
    ProfileDto result = userService.updateProfile(TEST_USER_ID, request, image);

    assertThat(result).isNotNull();
    verify(s3Service, never()).deleteFile(anyString());
    verify(s3Service).uploadProfileImage(TEST_USER_ID, image);
  }

  @Test
  @DisplayName("프로필 이미지 업데이트 - 기존 이미지 null인 경우")
  void updateProfile_ExistingImageIsNull() {
    MultipartFile image = mock(MultipartFile.class);
    when(image.isEmpty()).thenReturn(false);

    String newImageUrl = "https://bucket.s3.amazonaws.com/profile/new.jpg";
    when(s3Service.uploadProfileImage(TEST_USER_ID, image)).thenReturn(newImageUrl);
    when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
    when(userRepository.save(testUser)).thenReturn(testUser);

    ProfileUpdateRequest request = new ProfileUpdateRequest(null, null, null, null, null);
    ProfileDto result = userService.updateProfile(TEST_USER_ID, request, image);

    assertThat(result).isNotNull();
    verify(s3Service, never()).deleteFile(anyString());
    verify(s3Service).uploadProfileImage(TEST_USER_ID, image);
  }

  @Test
  @DisplayName("비밀번호 변경 - 임시 비밀번호 클리어")
  void changePassword_ClearTempPassword() {
    String newPassword = "newPassword123";

    testUser.setTempPassword("tempPassword", java.time.LocalDateTime.now().plusHours(1));

    when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
    when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
    when(userRepository.save(testUser)).thenReturn(testUser);

    userService.changePassword(TEST_USER_ID, newPassword);

    verify(passwordEncoder).encode(newPassword);
    verify(userRepository).save(testUser);
  }

  @Test
  @DisplayName("위치별 사용자 조회 - 빈 결과")
  void findUserIdsByLocation_EmptyResult() {
    int x = 60, y = 127;
    when(userRepository.findByLocationXAndLocationY(x, y)).thenReturn(List.of());

    List<UUID> result = userService.findUserIdsByLocation(x, y);

    assertThat(result).isEmpty();
    verify(userRepository).findByLocationXAndLocationY(x, y);
  }

  @Test
  @DisplayName("프로필 업데이트 - 위치 정보와 로그")
  void updateProfile_LocationWithLogging() {
    ProfileUpdateRequest.LocationRequest locationRequest =
        new ProfileUpdateRequest.LocationRequest(37.5665, 126.9780, 60, 127, List.of("서울", "중구"));
    ProfileUpdateRequest request = new ProfileUpdateRequest(null, null, null, locationRequest, null);

    when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
    when(userRepository.save(testUser)).thenReturn(testUser);

    ProfileDto result = userService.updateProfile(TEST_USER_ID, request, null);

    assertThat(result).isNotNull();
    verify(userRepository).save(testUser);
  }
}
