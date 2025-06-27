package com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


//프로토타입에서는 예외발생시 별다른 상태코드나 메세지를 반환하지 않아서 우선 임의지정.
public record CommentCreateRequest(
	@NotBlank(message = "유효한 요청이 아닙니다: 피드 ID 누락")
	UUID feedId,
	@NotBlank(message = "유효한 요청이 아닙니다: 작성자 ID 누락")
	UUID authorId,

	/*	content 필드의 경우, 프로토타입에 따르면 varchar(255바이트)가 넘어가면 500오류반환하는데,
	이 부분 추후 프로토타입 변경이 있을 수 있으므로
	우선 글자수를 85자로 제한하였다.(이모지는 3바이트이므로 255/3)
	또한 프로토타입에서는 공백으로 요청해도 정상등록되나, 마찬가지로 1자 이상으로 제한하였음. */
	@NotBlank(message = "유효한 요청이 아닙니다: 댓글 내용이 존재하지 않음.")
	@Size(min=1, max=85, message = "85자 이하로 입력해주세요.")
	String content
) {
}
