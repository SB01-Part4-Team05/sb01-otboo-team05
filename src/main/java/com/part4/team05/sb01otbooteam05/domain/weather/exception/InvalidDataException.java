package com.part4.team05.sb01otbooteam05.domain.weather.exception;

import com.part4.team05.sb01otbooteam05.exception.ErrorCode;

public class InvalidDataException extends WeatherException {

  public InvalidDataException(String message) {
    super(ErrorCode.INVALID_DATA);
  }
}
