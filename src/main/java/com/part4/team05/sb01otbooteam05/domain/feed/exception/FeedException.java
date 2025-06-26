package com.part4.team05.sb01otbooteam05.domain.feed.exception;

import com.part4.team05.sb01otbooteam05.exception.ErrorCode;
import com.part4.team05.sb01otbooteam05.exception.OtbooException;

public class FeedException extends OtbooException {
	public FeedException(ErrorCode errorCode) {
		super(errorCode);
	}

    public FeedException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}
