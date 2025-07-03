package com.part4.team05.sb01otbooteam05.domain.weather.Exception;

import com.part4.team05.sb01otbooteam05.exception.ErrorCode;
import com.part4.team05.sb01otbooteam05.exception.OtbooException;

public class WeatherException extends OtbooException {
	public WeatherException(ErrorCode errorCode) {
		super(errorCode);
	}

	public WeatherException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}

}
