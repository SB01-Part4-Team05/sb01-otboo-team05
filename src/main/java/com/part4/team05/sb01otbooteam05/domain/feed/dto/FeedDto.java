package com.part4.team05.sb01otbooteam05.domain.feed.dto;

import com.part4.team05.sb01otbooteam05.domain.ootd.dto.OotdDto;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// todo 캐싱으로 count 등 계산 효율화 필요
@Builder
public record FeedDto(
        @NotNull
        UUID id,

        @NotNull
        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        @NotNull
        @Valid
        AuthorDto author,

        @NotNull
        @Valid
        WeatherDto weather,

        @NotNull
        @Valid
        List<OotdDto> ootds,

        @Size(min = 0, max = 85) // 최대길이 임의지정. (피드생성 및 수정 요청 Dto 참고)
        String content,

        @NotNull
        Long likeCount,

        @NotNull
        Integer commentCount,

        @NotNull
        Boolean likedByMe
) {
}
