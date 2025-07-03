package com.part4.team05.sb01otbooteam05.domain.user.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.part4.team05.sb01otbooteam05.domain.user.dto.UserCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserDto;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.exception.UserNotFoundException;
import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public UserDto signUp(UserCreateRequest request) {
		log.info("회원가입 시도: email={}", request.getEmail());

		// 이메일 중복 확인
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("이미 사용 중인 이메일입니다"); //todo 추후 예외처리 예정
		}

		// 비밀번호 암호화
		String encodedPassword = passwordEncoder.encode(request.getPassword());

		// 사용자 생성 및 저장
		User user = User.createUser(
			request.getEmail(),
			request.getName(),
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
}
