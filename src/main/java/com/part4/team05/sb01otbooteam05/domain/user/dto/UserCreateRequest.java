package com.part4.team05.sb01otbooteam05.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

  //프로토타입 참고 후 지정함
  @NotBlank(message = "이름은 필수입니다")
  @Size(min = 1, max = 20, message = "이름은 1자 이상 20자 이하여야 합니다") //프로토타입상 최대는 정해져 있지 않지만 지정함
  private String name;

  @NotBlank(message = "이메일을 입력하십시오")
  @Email(message = "올바른 이메일 형식이 아닙니다")
  private String email;

  @NotBlank(message = "비밀번호를 입력하십시오")
  @Size(min = 6, message = "비밀번호는 6자 이상이어야 합니다")
  private String password;
}