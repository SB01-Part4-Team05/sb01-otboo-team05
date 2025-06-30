package com.part4.team05.sb01otbooteam05.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// User 관련 에러 코드
	USER_NOT_FOUND("해당 유저를 찾을 수 없습니다."),

	// Feed 관련 에러 코드
	FEED_NOT_FOUND("해당 피드를 찾을 수 없습니다."),

	// Notification 관련 에러 코드
	NOTIFICATION_NOT_FOUND("알림이 존재하지 않습니다."),


	// 서버 관련 에러 코드
	INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다."),
	INVALID_REQUEST("잘못된 요청입니다.");

	private final String message;
}
