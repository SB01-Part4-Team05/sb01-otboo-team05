package com.part4.team05.sb01otbooteam05.domain.clothes.Exception;

import com.part4.team05.sb01otbooteam05.exception.ErrorCode;
import com.part4.team05.sb01otbooteam05.exception.OtbooException;

public class ClothesException extends OtbooException {
	public ClothesException(ErrorCode errorCode) {
		super(errorCode);
	}

	public ClothesException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}

}
