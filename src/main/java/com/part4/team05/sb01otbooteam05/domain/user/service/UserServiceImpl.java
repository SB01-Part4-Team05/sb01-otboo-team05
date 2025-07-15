package com.part4.team05.sb01otbooteam05.domain.user.service;

import com.part4.team05.sb01otbooteam05.domain.user.exception.EmailAlreadyExistsException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.part4.team05.sb01otbooteam05.domain.user.dto.UserCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserDto;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.exception.UserNotFoundException;
import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;
import com.part4.team05.sb01otbooteam05.domain.user.dto.ProfileDto;
import com.part4.team05.sb01otbooteam05.domain.user.dto.ProfileUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.user.entity.GenderType;
import com.part4.team05.sb01otbooteam05.domain.user.util.LccGridConverter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final KakaoApiService kakaoApiService;

  // 파일 업로드 경로 (프로필 이미지)
  @Value("${file.upload-dir:uploads/profile}")
  private String uploadDir;


  /**
   * 회원가입
   */
  @Override
  @Transactional
  public UserDto signUp(UserCreateRequest request) {
    log.info("회원가입 시도: email={}", request.email());

    // 이메일 중복 확인
    if (userRepository.existsByEmail(request.email())) {
      throw new EmailAlreadyExistsException();
    }

    // 비밀번호 암호화
    String encodedPassword = passwordEncoder.encode(request.password());

    // 사용자 생성 및 저장
    User user = User.createUser(
        request.email(),
        request.name(),
        encodedPassword
    );

    User savedUser = userRepository.save(user);
    log.info("회원가입 성공: userId={}, email={}", savedUser.getId(), savedUser.getEmail());

    return UserDto.from(savedUser);
  }

  @Override
  @Transactional(readOnly = true)
  public User getUserEntityByIdOrThrow(UUID userId) {
    return userRepository.findById(userId).orElseThrow(() -> UserNotFoundException.withId(userId));
  }

  /**
   * 프로필 조회
   */
  @Override
  @Transactional(readOnly = true)
  public ProfileDto getProfile(UUID userId) {
    log.info("프로필 조회: userId={}", userId);

    User user = getUserEntityByIdOrThrow(userId);

    return ProfileDto.from(user);
  }

  /**
   * 프로필 업데이트
   */
  @Override
  @Transactional
  public ProfileDto updateProfile(UUID userId, ProfileUpdateRequest request, MultipartFile image) {
    log.info("프로필 업데이트: userId={}", userId);

    User user = getUserEntityByIdOrThrow(userId);

    // 이름 업데이트
    if (request.name() != null) {
      user.updateName(request.name());
    }

    // 성별 업데이트
    if (request.gender() != null) {
      try {
        GenderType genderType = GenderType.valueOf(request.gender());
        user.updateGender(genderType);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("잘못된 성별 값입니다: " + request.gender());
      }
    }

    // 생년월일 업데이트
    if (request.birthDate() != null) {
      user.updateBirthDate(request.birthDate());
    }

    // 위치 정보 업데이트
    if (request.location() != null) {
      ProfileUpdateRequest.LocationRequest loc = request.location();

      // 위도,경도를 가지고 x, y 계산
      LccGridConverter.XY gridXY = LccGridConverter.toGrid(loc.latitude(), loc.longitude());
      log.info("위경도({},{}) → 변환 결과 x={}, y={}",
          loc.latitude(), loc.longitude(), gridXY.x, gridXY.y);

      // 카카오 API를 통해 지역명 조회
      List<String> locationNames = kakaoApiService.getLocationNames(loc.latitude(), loc.longitude());
      log.info("조회된 지역명 목록: {}", locationNames);

      // 계산된 값들로 업데이트
      user.updateLocation(
          loc.latitude(),
          loc.longitude(),
          gridXY.x,
          gridXY.y,
          locationNames
      );
    }

    // 온도 민감도 업데이트
    if (request.temperatureSensitivity() != null) {
      user.updateTemperatureSensitivity(request.temperatureSensitivity());
    }

    // 프로필 이미지 업데이트
    if (image != null && !image.isEmpty()) {
      String imageUrl = saveProfileImage(userId, image);
      user.updateProfileImageUrl(imageUrl);
    }

    // 변경사항 저장
    User updatedUser = userRepository.save(user);
    log.info("프로필 업데이트 완료: userId={}", userId);

    return ProfileDto.from(updatedUser);
  }

  /**
   * 프로필 이미지를 저장하고 URL을 반환
   */
  private String saveProfileImage(UUID userId, MultipartFile image) {
    try {
      // 업로드 디렉토리 생성
      Path uploadPath = Paths.get(uploadDir);
      if (!Files.exists(uploadPath)) {
        Files.createDirectories(uploadPath);
      }

      // 파일 확장자 추출
      String originalFilename = image.getOriginalFilename();
      String extension = "";
      if (originalFilename != null && originalFilename.contains(".")) {
        extension = originalFilename.substring(originalFilename.lastIndexOf("."));
      }

      // 고유한 파일명 생성 (userId + 타임스탬프 + 확장자)
      String filename = userId.toString() + "_" + System.currentTimeMillis() + extension;

      // 파일 저장
      Path filePath = uploadPath.resolve(filename);
      Files.copy(image.getInputStream(), filePath);

      // URL 반환
      return "/uploads/profile/" + filename;

    } catch (IOException e) {
      log.error("프로필 이미지 저장 실패: userId={}", userId, e);
      throw new RuntimeException("프로필 이미지 저장에 실패했습니다", e);
    }
  }

  @Override
  @Transactional
  public void changePassword(UUID userId, String newPassword) {
    User user = getUserEntityByIdOrThrow(userId);

    // 비밀번호 암호화해서 저장
    user.setPassword(passwordEncoder.encode(newPassword));

    // 임시비밀번호 상태 해제(클리어)
    user.clearTempPassword();

    userRepository.save(user);
  }

}
