package com.part4.team05.sb01otbooteam05.domain.user.repository;

import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
  // 이메일 중복 확인
  boolean existsByEmail(String email);

  // 사용자 조회
  Optional<User> findById(UUID userId);

  // 이메일로 사용자 조회
  Optional<User> findByEmail(String email);

  // 등록된 위치 정보 중복,null 제외 후 조회
  @Query("SELECT DISTINCT u.locationX, u.locationY FROM User u WHERE u.locationX IS NOT NULL AND u.locationY IS NOT NULL")
  List<Object[]> findDistinctLocations();

  Optional<User> findByProviderAndProviderId(String provider, String providerId);

  // 특정 x,y(위치) 값을 가지는 유저의 id 조회
  List<User> findByLocationXAndLocationY(Integer locationX, Integer locationY);

  @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'ADMIN' AND u.locked = false")
  long countActiveAdmins();
}
