package com.part4.team05.sb01otbooteam05.domain.clothes.Exception;

import java.util.UUID;

import com.part4.team05.sb01otbooteam05.exception.ErrorCode;

public class ClothesNotFoundException extends ClothesException {
  public ClothesNotFoundException() {
    super(ErrorCode.CLOTHES_NOT_FOUND);
  }

  public static ClothesNotFoundException withId(UUID id) {
    ClothesNotFoundException exception = new ClothesNotFoundException();
    exception.addDetail("id", id);
    return exception;
  }
}
