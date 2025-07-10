package com.part4.team05.sb01otbooteam05.domain.auth.exception;

import com.part4.team05.sb01otbooteam05.exception.ErrorCode;
import com.part4.team05.sb01otbooteam05.exception.OtbooException;

public class InvalidTokenException extends OtbooException {

  public InvalidTokenException() {
    super(ErrorCode.INVALID_TOKEN);
  }
}
