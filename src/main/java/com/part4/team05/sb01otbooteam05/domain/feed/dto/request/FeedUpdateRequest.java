package com.part4.team05.sb01otbooteam05.domain.feed.dto.request;

import jakarta.validation.constraints.Size;

//프로토타입에서는 예외발생시 별다른 상태코드나 메세지를 반환하지 않아서 우선 임의지정.
public record FeedUpdateRequest(

	/*	content 필드의 경우, 프로토타입에 따르면 varchar(255바이트)가 넘어가면 500오류반환하는데,
	이 부분 추후 프로토타입 변경이 있을 수 있으므로
	우선 글자수를 85자로 제한하였다.(이모지는 3바이트이므로 255/3)
	글 작성 없이 옷만 올리는 경우(==content 필드의 값이 null일 경우) 예외 반환 없이 검증을 통과한다*/
	@Size(min = 0, max = 85)
	String content
) {
}
