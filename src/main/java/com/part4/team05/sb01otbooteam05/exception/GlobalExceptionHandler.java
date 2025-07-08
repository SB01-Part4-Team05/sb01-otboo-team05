package com.part4.team05.sb01otbooteam05.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	// Otboo에서 명시한 예외 발생 시
	@ExceptionHandler(OtbooException.class)
	protected ResponseEntity<ErrorResponse> handleOtbooException(OtbooException exception) {
		log.error("커스텀 예외 발생: code={}, message = {}", exception.getErrorCode(), exception.getMessage());
		HttpStatus status = determineHttpStatus(exception);
		ErrorResponse errorResponse = new ErrorResponse(exception, status.value());
		return ResponseEntity.status(status).body(errorResponse);
	}

	// Otboo를 상속받지 않은, 예상치 못한 예외 발생 시
	@ExceptionHandler(RuntimeException.class)
	protected ResponseEntity<ErrorResponse> handleRuntimeException(Exception exception) {
		log.error("런타임 예외 발생: message = {}",exception.getMessage());

		ErrorResponse errorResponse = new ErrorResponse(exception, HttpStatus.INTERNAL_SERVER_ERROR.value());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	}

	// 잘못된 인수 전달 시
	@ExceptionHandler(IllegalArgumentException.class)
	protected ResponseEntity<ErrorResponse> handleIllegalArgumentException(Exception exception) {
		log.error("잘못된 인수가 전달되었습니다:  message = {}", exception.getMessage());
		ErrorResponse errorResponse = new ErrorResponse(exception, HttpStatus.BAD_REQUEST.value());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	// @Valid, @Validated 유효성 검사 실패 시
	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
		log.error("유효성 검사에 실패하였습니다.:  message = {}", exception.getMessage());
		List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
		ErrorResponse errorResponse = new ErrorResponse(fieldErrors, HttpStatus.BAD_REQUEST.value());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	// 파라미터 타입이 맞지 않아 매핑 실패 시
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	protected ResponseEntity<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exception) {
		log.error("잘못된 타입의 파라미터가 전달되었습니다.:  message = {}", exception.getMessage());
		ErrorResponse errorResponse = new ErrorResponse(exception, HttpStatus.BAD_REQUEST.value());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	// 필수값으로 설정되어있는 파라미터가 없을 시
	@ExceptionHandler(MissingServletRequestParameterException.class)
	protected ResponseEntity<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException exception) {
		log.error("필수 파라미터가 누락되었습니다.:  message = {}", exception.getMessage());
		ErrorResponse errorResponse = new ErrorResponse(exception, HttpStatus.BAD_REQUEST.value());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}



	private HttpStatus determineHttpStatus(OtbooException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		return switch (errorCode) {
			// 404 Not Found
			case USER_NOT_FOUND,
				 FEED_NOT_FOUND,
				 CLOTHES_NOT_FOUND,
				 WEATHER_NOT_FOUND -> HttpStatus.NOT_FOUND;

			// 400 Bad Request
			case INVALID_REQUEST -> HttpStatus.BAD_REQUEST;

			// 500 Internal Server Error
			case INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;

			// 안정성을 위해 500 Internal Server Error를 기본값으로 설정
			default -> HttpStatus.INTERNAL_SERVER_ERROR;
		};
	}

}
