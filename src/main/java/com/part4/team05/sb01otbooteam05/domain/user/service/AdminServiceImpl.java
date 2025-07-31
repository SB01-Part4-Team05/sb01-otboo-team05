package com.part4.team05.sb01otbooteam05.domain.user.service;

import com.part4.team05.sb01otbooteam05.domain.auth.repository.RefreshTokenRepository;
import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.notification.entity.NotificationLevel;
import com.part4.team05.sb01otbooteam05.domain.notification.service.NotificationService;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserDto;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserLockUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserRoleUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.entity.UserRole;
import com.part4.team05.sb01otbooteam05.domain.user.exception.UserNotFoundException;
import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final NotificationService notificationService;

  // 슈퍼 어드민 판별 헬퍼 메서드
  private boolean isSuperAdmin(User user) {
    return "LOCAL".equals(user.getProvider()) &&
        user.getEmail() != null &&
        user.getEmail().endsWith("@otboo.com") &&
        user.getRole() == UserRole.ADMIN;
  }

  private long countActiveAdmins() {
    return userRepository.countActiveAdmins();
  }

  // 현재 로그인한 관리자 ID 가져오기
  private UUID getCurrentAdminId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      return userDetails.getUserId();
    }
    throw new SecurityException("인증되지 않은 요청입니다.");
  }

  /**
   * 사용자 목록 조회 (커서 기반 페이지네이션)
   */
  @Override
  @Transactional(readOnly = true)
  public UserDtoCursorResponse getUsers(
      String cursor,
      UUID idAfter,
      Integer limit,
      String sortBy,
      String sortDirection,
      String emailLike,
      UserRole roleEqual,
      Boolean locked) {

    log.info("사용자 목록 조회: limit={}, cursor={}", limit, cursor);

    // 기본값 설정
    if (limit == null) limit = 20;
    if (sortBy == null) sortBy = "createdAt";
    if (sortDirection == null) sortDirection = "DESCENDING";

    // 정렬 설정
    Direction direction = "ASCENDING".equals(sortDirection) ? Direction.ASC : Direction.DESC;
    Sort sort = Sort.by(direction, sortBy);

    // 페이지 설정
    Pageable pageable = PageRequest.of(0, limit + 1, sort); // +1로 다음 페이지 존재 여부 확인

    // 검색 조건 설정
    Specification<User> spec = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

    // 커서가 있으면 해당 ID 이후 데이터만 조회
    if (idAfter != null) {
      spec = spec.and((root, query, cb) ->
          cb.greaterThan(root.get("id"), idAfter));
    }

    // 이메일 검색 조건
    if (emailLike != null && !emailLike.isEmpty()) {
      spec = spec.and((root, query, cb) ->
          cb.like(cb.lower(root.get("email")), "%" + emailLike.toLowerCase() + "%"));
    }

    // 권한 검색 조건
    if (roleEqual != null) {
      spec = spec.and((root, query, cb) ->
          cb.equal(root.get("role"), roleEqual));
    }

    // 잠금 상태 검색 조건
    if (locked != null) {
      spec = spec.and((root, query, cb) ->
          cb.equal(root.get("locked"), locked));
    }

    // 데이터 조회
    Page<User> userPage = userRepository.findAll(spec, pageable);
    List<User> users = userPage.getContent();

    // 다음 페이지 존재 여부 확인
    boolean hasNext = users.size() > limit;
    if (hasNext) {
      users = users.subList(0, limit); // 실제 반환할 데이터만 남김
    }

    // User 엔티티를 UserDto로 변환
    List<UserDto> userDtos = users.stream()
        .map(UserDto::from)
        .collect(Collectors.toList());

    // 다음 커서 및 ID 설정
    String nextCursor = null;
    UUID nextIdAfter = null;
    if (hasNext && !users.isEmpty()) {
      User lastUser = users.get(users.size() - 1);
      nextIdAfter = lastUser.getId();
      nextCursor = lastUser.getId().toString(); // 간단하게 ID를 커서로 사용
    }

    // 전체 개수 조회
    long totalCount = userRepository.count(spec);

    return UserDtoCursorResponse.builder()
        .data(userDtos)
        .nextCursor(nextCursor)
        .nextIdAfter(nextIdAfter)
        .hasNext(hasNext)
        .totalCount(totalCount)
        .sortBy(sortBy)
        .sortDirection(sortDirection)
        .build();
  }

  /**
   * 사용자 권한 변경
   */
  @Override
  @Transactional
  public UserDto updateUserRole(UUID userId, UserRoleUpdateRequest request) {
    UUID currentAdminId = getCurrentAdminId();

    log.info("사용자 권한 변경: userId={}, newRole={}, requesterId={}", userId, request.role(), currentAdminId);

    User targetUser = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));

    // 자기 자신 수정 방지
    if (userId.equals(currentAdminId)) {
      log.warn("자기 자신의 권한 변경 시도 차단: userId={}", userId);
      throw new SecurityException("자기 자신의 권한은 변경할 수 없습니다.");
    }

    // 슈퍼 어드민 보호 (헬퍼 메서드 사용)
    if (isSuperAdmin(targetUser)) {
      log.warn("슈퍼 어드민 권한 변경 시도 차단: userId={}", userId);
      throw new SecurityException("슈퍼 어드민의 권한은 변경할 수 없습니다.");
    }

    // 마지막 관리자 보호
    if (targetUser.getRole() == UserRole.ADMIN && request.role() != UserRole.ADMIN) {
      long adminCount = countActiveAdmins();
      if (adminCount <= 1) {
        log.warn("마지막 관리자 권한 제거 시도 차단: userId={}, adminCount={}", userId, adminCount);
        throw new SecurityException("시스템에 최소 1명의 관리자가 있어야 합니다.");
      }
    }

    targetUser.updateRole(request.role());
    User updatedUser = userRepository.save(targetUser);

    refreshTokenRepository.revokeAllByUserId(userId);
    log.info("권한 변경 완료 및 강제 로그아웃 처리: userId={}", userId);

    // 권한 변경 알림 전송
     try {
       notificationService.createAndSendNotification(
               updatedUser.getId(),
               "권한이 변경되었습니다",
               "관리자에 의해 귀하의 권한이 " + request.role().name() + "(으)로 변경되었습니다.",
               NotificationLevel.INFO
       );
     } catch (Exception e) {
       log.warn("권한 변경 알림 전송 실패: userId={}", updatedUser.getId(), e);
     }

    return UserDto.from(updatedUser);
  }

  /**
   * 사용자 계정 잠금 상태 변경
   */
  @Override
  @Transactional
  public UUID updateUserLockStatus(UUID userId, UserLockUpdateRequest request) {
    UUID currentAdminId = getCurrentAdminId();

    log.info("사용자 잠금 상태 변경: userId={}, locked={}, requesterId={}", userId, request.locked(), currentAdminId);

    User targetUser = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));

    //  자기 자신 잠금 방지
    if (userId.equals(currentAdminId)) {
      log.warn("자기 자신의 계정 잠금 시도 차단: userId={}", userId);
      throw new SecurityException("자기 자신의 계정은 잠글 수 없습니다.");
    }

    // 슈퍼 어드민 보호
    if (isSuperAdmin(targetUser)) {
      log.warn("슈퍼 어드민 잠금 시도 차단: userId={}", userId);
      throw new SecurityException("슈퍼 어드민 계정은 잠글 수 없습니다.");
    }

    // 마지막 관리자 보호
    if (request.locked() && targetUser.getRole() == UserRole.ADMIN) {
      long activeAdminCount = countActiveAdmins();
      if (activeAdminCount <= 1) {
        log.warn("마지막 활성 관리자 잠금 시도 차단: userId={}, activeAdminCount={}", userId, activeAdminCount);
        throw new SecurityException("시스템에 최소 1명의 활성 관리자가 있어야 합니다.");
      }
    }

    targetUser.updateLocked(request.locked());
    userRepository.save(targetUser);

    if (request.locked()) {
      refreshTokenRepository.revokeAllByUserId(userId);
      log.info("계정 잠금 완료 및 강제 로그아웃 처리: userId={}", userId);
    }

    return userId;
  }
}
