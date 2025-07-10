package com.part4.team05.sb01otbooteam05.domain.user.repository;

import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  // 이메일 중복 확인
  boolean existsByEmail(String email);

  // 사용자 조회
  Optional<User> findById(UUID userId);

  // 이메일로 사용자 조회
  Optional<User> findByEmail(String email);

}
