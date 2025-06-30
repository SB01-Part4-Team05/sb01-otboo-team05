package com.part4.team05.sb01otbooteam05.domain.notification.exception;

import com.part4.team05.sb01otbooteam05.exception.ErrorCode;
import com.part4.team05.sb01otbooteam05.exception.OtbooException;

public class NotificationNotFoundException extends OtbooException {
    public NotificationNotFoundException() {
        super(ErrorCode.NOTIFICATION_NOT_FOUND);
    }
}
