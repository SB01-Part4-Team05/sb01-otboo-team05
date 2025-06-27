package com.part4.team05.sb01otbooteam05.domain.feed.dto.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

//프로토타입에는 예외발생시 별다른 상태코드나 메세지를 반환하지 않아서 우선 임의지정.
public record FeedCreateRequest(
	@NotNull(message = "유효한 요청이 아닙니다: 작성자 ID 누락")
	UUID authorId,
	@NotNull(message = "유효한 요청이 아닙니다: 날씨 ID 누락")
	UUID weatherId,
	@NotNull(message = "유효한 요청이 아닙니다: 옷 ID 누락")
	@Size(min=1, max=10, message = "피드에는 최소 1장, 최대 10장의 옷이 등록되어야 합니다.")
	List<UUID> clothesIds,

/*	content 필드의 경우, 프로토타입에 따르면 varchar(255바이트)가 넘어가면 500오류반환하는데,
	이 부분 추후 프로토타입 변경이 있을 수 있으므로
	우선 글자수를 85자로 제한하였다.(이모지는 3바이트이므로 255/3)
	글 작성 없이 옷만 올리는 경우(==content 필드의 값이 null일 경우) 예외 반환 없이 검증을 통과한다*/
	@Size(min = 0, max = 85)
	String content

) {
}
