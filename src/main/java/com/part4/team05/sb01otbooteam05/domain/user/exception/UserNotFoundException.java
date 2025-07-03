package com.part4.team05.sb01otbooteam05.domain.user.exception;

import java.util.UUID;

import com.part4.team05.sb01otbooteam05.exception.ErrorCode;

public class UserNotFoundException extends UserException {
  public UserNotFoundException() {
    super(ErrorCode.USER_NOT_FOUND);
  }

  public static UserNotFoundException withId(UUID id) {
    UserNotFoundException exception = new UserNotFoundException();
    exception.addDetail("id", id);
    return exception;
  }
}
