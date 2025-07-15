package com.part4.team05.sb01otbooteam05.domain.follow.exception;

import com.part4.team05.sb01otbooteam05.exception.ErrorCode;
import com.part4.team05.sb01otbooteam05.exception.OtbooException;

public class FollowException extends OtbooException {
    public FollowException(ErrorCode errorCode) {
        super(errorCode);
    }
}
