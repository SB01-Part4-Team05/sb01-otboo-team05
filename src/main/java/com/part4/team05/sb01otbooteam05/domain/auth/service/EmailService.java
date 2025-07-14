package com.part4.team05.sb01otbooteam05.domain.auth.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
  private final JavaMailSender mailSender;

  public void sendTempPassword(String to, String tempPassword, LocalDateTime expireAt) {
    String subject = "[OTBOO] 임시 비밀번호 안내";
    String text = """
            안녕하세요.
            요청하신 임시 비밀번호는 아래와 같습니다.

            임시 비밀번호: %s
            만료 시간: %s

            로그인 후 반드시 새 비밀번호로 변경해 주세요.
            """.formatted(tempPassword, expireAt);

    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject(subject);
    message.setText(text);

    mailSender.send(message);
  }
}
