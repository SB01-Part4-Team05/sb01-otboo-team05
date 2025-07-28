package com.part4.team05.sb01otbooteam05.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Slf4j
@ControllerAdvice
public class WebSocketExceptionHandler {

    @MessageExceptionHandler(OtbooException.class)
    @SendToUser("/queue/errors") // 구독 주소
    public ErrorResponse handleOtbooException(OtbooException e) {
        log.error("WebSocket 예외 발생: {}", e.getMessage());
        return new ErrorResponse(e, 400);
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public ErrorResponse handleAnyException(Exception e) {
        log.error("WebSocket 기타 예외 발생: {}", e.getMessage(), e);
        return new ErrorResponse(e, 500);
    }
}
