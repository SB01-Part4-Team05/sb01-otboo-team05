package com.part4.team05.sb01otbooteam05.domain.user.exception;

import com.part4.team05.sb01otbooteam05.exception.ErrorCode;

public class EmailAlreadyExistsException extends UserException {
  public EmailAlreadyExistsException() {
    super(ErrorCode.EMAIL_ALREADY_EXISTS);
    this.addDetail("reason", "해당 이메일로 가입된 계정이 이미 존재합니다.");
  }
}
