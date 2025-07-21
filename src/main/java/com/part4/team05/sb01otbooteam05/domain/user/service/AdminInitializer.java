package com.part4.team05.sb01otbooteam05.domain.user.service;

import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.entity.UserRole;
import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${admin.email}")
  private String adminEmail;

  @Value("${admin.password}")
  private String adminPassword;

  @Value("${admin.name}")
  private String adminName;

  @Override
  @Transactional
  public void run(String... args) {
    // 어드민 계정이 이미 존재하는지 확인
    if (!userRepository.existsByEmail(adminEmail)) {
      // 어드민 계정 생성
      User admin = User.builder()
          .email(adminEmail)
          .name(adminName)
          .password(passwordEncoder.encode(adminPassword))
          .role(UserRole.ADMIN)
          .locked(false)
          .provider("LOCAL")
          .isTempPassword(false)
          .build();

      userRepository.save(admin);
      log.info("어드민 계정이 생성되었습니다: email={}", adminEmail);
    } else {
      log.info("어드민 계정이 이미 존재합니다: email={}", adminEmail);
    }
  }
}
