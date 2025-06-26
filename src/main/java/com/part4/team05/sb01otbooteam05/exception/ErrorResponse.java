package com.part4.team05.sb01otbooteam05.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ErrorResponse {

	private final Instant timestamp;
	private final String code;
	private final String message;
	private final Map<String, Object> details;
	private final String exceptionType;
	private final int status;

	// OtbooException를 통한 명시적인 에러처리
	public ErrorResponse(OtbooException exception, int status) {
		this(Instant.now(), exception.getErrorCode().name(), exception.getMessage(),
			exception.getDetails(), exception.getClass().getSimpleName(), status);
	}

	// OtbooException에 명시되지 않은 예외에 대한 기본 처리
	public ErrorResponse(Exception exception, int status) {
		this(Instant.now(), exception.getClass().getSimpleName(), exception.getMessage(), new HashMap<>(),
			exception.getClass().getSimpleName(), status);
	}

	// @Valid, @Validated 유효성 검사 실패 시 발생하는 예외에 대한 기본 처리
	public ErrorResponse(List<FieldError> fieldErrors, int status) {
		this(
			Instant.now(),
			"VALIDATION_ERROR",
			"요청 데이터가 유효하지 않습니다.",
			convertFieldErrorsToDetails(fieldErrors),
			MethodArgumentNotValidException.class.getSimpleName(),
			status
		);
	}


	// @Valid, @Validated 으로 인한 에러가 가진 에러 세부정보 필드를 응답에 적합한 형태로 변환해주는 메서드
	private static Map<String, Object> convertFieldErrorsToDetails(List<FieldError> fieldErrors) {
		Map<String, Object> details = new HashMap<>();
		for (FieldError error : fieldErrors) {
			details.put(error.getField(), error.getDefaultMessage());
		}
		return details;
	}
}
