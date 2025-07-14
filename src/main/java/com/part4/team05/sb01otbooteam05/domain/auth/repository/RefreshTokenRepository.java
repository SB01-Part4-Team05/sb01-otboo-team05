package com.part4.team05.sb01otbooteam05.domain.auth.repository;

import com.part4.team05.sb01otbooteam05.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

  // 토큰으로 조회 (사용자 정보 함께 로드)
  @Query("SELECT rt FROM RefreshToken rt JOIN FETCH rt.user WHERE rt.token = :token")
  Optional<RefreshToken> findByTokenWithUser(@Param("token") String token);

  // 사용자의 모든 토큰 무효화
  @Modifying
  @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId")
  void revokeAllByUserId(@Param("userId") UUID userId);

  // 사용자 ID로 토큰 존재 여부 확인
  boolean existsByUserIdAndRevokedFalse(UUID userId);
}
