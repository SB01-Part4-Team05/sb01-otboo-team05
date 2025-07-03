package com.part4.team05.sb01otbooteam05.domain.weather.exception;

import java.util.UUID;

import com.part4.team05.sb01otbooteam05.exception.ErrorCode;

public class WeatherNotFoundException extends
    com.part4.team05.sb01otbooteam05.domain.weather.exception.WeatherException {
  public WeatherNotFoundException() {
    super(ErrorCode.WEATHER_NOT_FOUND);
  }

  public static WeatherNotFoundException withId(UUID id) {
    WeatherNotFoundException exception = new WeatherNotFoundException();
    exception.addDetail("id", id);
    return exception;
  }
}
