package com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record FindCommentsRequest(
        @NotNull(message = "유효한 요청이 아닙니다: feed ID 누락")
        UUID feedId,

        LocalDateTime cursor,
        UUID idAfter,

        @NotNull(message = "유효한 요청이 아닙니다: limit 값 누락")
        @Min(value = 0, message = "limit은 음수일 수 없습니다.")
        Integer limit
) {
}
