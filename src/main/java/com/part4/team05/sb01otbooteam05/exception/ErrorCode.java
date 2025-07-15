package com.part4.team05.sb01otbooteam05.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// User 관련 에러 코드
	USER_NOT_FOUND("해당 유저를 찾을 수 없습니다."),
	EMAIL_ALREADY_EXISTS("이미 사용 중인 이메일입니다."),

	//Auth 관련 에러 코드
	UNAUTHORIZED("인증이 필요합니다"),
	INVALID_TOKEN("유효하지 않은 토큰입니다"),

	// Feed 관련 에러 코드
	FEED_NOT_FOUND("해당 피드를 찾을 수 없습니다."),

	// Clothes 관련 에러 코드
	CLOTHES_NOT_FOUND("해당 옷을 찾을 수 없습니다."),
	// Weather 관련 에러 코드
	WEATHER_NOT_FOUND("해당 날씨를 찾을 수 없습니다."),
	INVALID_DATA("잘못된 값입니다."),
	WEATHER_BATCH_FAILED("배치 실행 실패하였습니다."),

    // Notification 관련 에러 코드
	NOTIFICATION_NOT_FOUND("알림이 존재하지 않습니다."),

	// follow 관련 에러 코드
	FOLLOW_SELF_NOT_ALLOWED("자기 자신을 팔로우할 수 없습니다."),
	ALREADY_FOLLOWED("이미 팔로우한 사용자입니다."),

	// 서버 관련 에러 코드
	INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다."),
	INVALID_REQUEST("잘못된 요청입니다.");

	private final String message;
}
