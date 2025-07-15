package com.part4.team05.sb01otbooteam05.domain.weather.exception;

import com.part4.team05.sb01otbooteam05.exception.ErrorCode;

public class WeatherBatchException extends WeatherException {

  public WeatherBatchException(String message) {
    super(ErrorCode.WEATHER_BATCH_FAILED);
  }
}
