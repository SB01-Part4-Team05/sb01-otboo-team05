package com.part4.team05.sb01otbooteam05.domain.auth.exception;

import com.part4.team05.sb01otbooteam05.exception.ErrorCode;
import com.part4.team05.sb01otbooteam05.exception.OtbooException;

public class UnauthorizedException extends OtbooException {

  public UnauthorizedException() {
    super(ErrorCode.UNAUTHORIZED);
  }
}
