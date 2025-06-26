package com.part4.team05.sb01otbooteam05.domain.user.repository;

import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  // 이메일 중복 확인
  boolean existsByEmail(String email);
}