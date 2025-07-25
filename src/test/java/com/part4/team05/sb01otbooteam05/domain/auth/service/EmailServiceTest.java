package com.part4.team05.sb01otbooteam05.domain.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("이메일 서비스 핵심 테스트")
class EmailServiceTest {

  @Mock private JavaMailSender mailSender;
  @InjectMocks private EmailService emailService;

  private final String TEST_EMAIL = "test@example.com";
  private final String TEST_TEMP_PASSWORD = "temp123";
  private LocalDateTime expireTime;

  @BeforeEach
  void setUp() {
    expireTime = LocalDateTime.now().plusHours(2);
  }

  @Test
  @DisplayName("임시 비밀번호 이메일 전송 성공")
  void sendTempPassword_Success() {
    doNothing().when(mailSender).send(any(SimpleMailMessage.class));
    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    emailService.sendTempPassword(TEST_EMAIL, TEST_TEMP_PASSWORD, expireTime);

    verify(mailSender).send(messageCaptor.capture());

    SimpleMailMessage sentMessage = messageCaptor.getValue();
    assertThat(sentMessage.getTo()).contains(TEST_EMAIL);
    assertThat(sentMessage.getSubject()).isEqualTo("[OTBOO] 임시 비밀번호 안내");
    assertThat(sentMessage.getText()).contains(TEST_TEMP_PASSWORD);
  }

  @Test
  @DisplayName("임시 비밀번호 이메일 전송 실패 - null 파라미터")
  void sendTempPassword_NullParameters_ThrowsException() {
    assertThatThrownBy(() -> emailService.sendTempPassword(null, TEST_TEMP_PASSWORD, expireTime))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("이메일 전송에 필요한 정보가 누락되었습니다");

    verify(mailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("이메일 전송 실패 - JavaMailSender 예외")
  void sendTempPassword_MailSenderException_ThrowsRuntimeException() {
    RuntimeException mailException = new RuntimeException("Mail server error");
    doThrow(mailException).when(mailSender).send(any(SimpleMailMessage.class));

    assertThatThrownBy(() -> emailService.sendTempPassword(TEST_EMAIL, TEST_TEMP_PASSWORD, expireTime))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("이메일 전송에 실패했습니다")
        .hasCause(mailException);
  }
}
