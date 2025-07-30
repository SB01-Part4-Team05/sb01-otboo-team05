package com.part4.team05.sb01otbooteam05.domain.feed.dto.request;

import java.util.UUID;

import org.hibernate.query.SortDirection;

import com.part4.team05.sb01otbooteam05.domain.feed.enums.SortType;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.PrecipitationType;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.SkyStatusType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FindFeedsRequest(

        String cursor,
        UUID idAfter,

        @NotNull(message = "유효한 요청이 아닙니다: limit 값 누락")
        @Min(value = 0, message = "유효한 요청이 아닙니다: limit 값은 음수일 수 없습니다.")
        Integer limit,

        @NotNull(message = "유효한 요청이 아닙니다: 정렬 필드 누락")
        SortType sortBy,

        @NotNull(message = "유효한 요청이 아닙니다: 정렬 방향 누락")
        SortDirection sortDirection,

        String keywordLike,
        SkyStatusType skyStatusEqual,
        PrecipitationType precipitationTypeEqual,
        UUID authorIdEqual
) {
}
