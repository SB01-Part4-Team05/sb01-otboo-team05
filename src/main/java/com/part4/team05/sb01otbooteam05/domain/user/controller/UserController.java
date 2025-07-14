package com.part4.team05.sb01otbooteam05.domain.user.controller;

import com.part4.team05.sb01otbooteam05.domain.user.dto.ChangePasswordRequest;
import com.part4.team05.sb01otbooteam05.domain.user.dto.ProfileDto;
import com.part4.team05.sb01otbooteam05.domain.user.dto.ProfileUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserDto;
import com.part4.team05.sb01otbooteam05.domain.user.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  /**
   * 회원가입
   */
  @PostMapping
  public ResponseEntity<UserDto> signUp(@Valid @RequestBody UserCreateRequest request) {
    UserDto user = userService.signUp(request);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(user);
  }

  /**
   * 프로필 조회
   */
  @GetMapping("/{userId}/profiles")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ProfileDto> getProfile(@PathVariable UUID userId) {
    ProfileDto profile = userService.getProfile(userId);
    return ResponseEntity.ok(profile);
  }

  /**
   * 프로필 업데이트
   */
  @PatchMapping(value = "/{userId}/profiles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ProfileDto> updateProfile(
      @PathVariable UUID userId,
      @RequestPart(value = "request") @Valid ProfileUpdateRequest request,
      @RequestPart(value = "image", required = false) MultipartFile image) {

    ProfileDto updatedProfile = userService.updateProfile(userId, request, image);
    return ResponseEntity.ok(updatedProfile);
  }

  @PatchMapping("/{userId}/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void changePassword(
      @PathVariable UUID userId,
      @RequestBody ChangePasswordRequest request
  ) {
    userService.changePassword(userId, request.password());
  }
}
