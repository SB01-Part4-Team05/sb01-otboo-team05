package com.part4.team05.sb01otbooteam05.domain.feed.dto;

import com.part4.team05.sb01otbooteam05.domain.feed.enums.SortType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.hibernate.query.SortDirection;

import java.util.List;
import java.util.UUID;

@Builder
public record FeedDtoCursorResponse(

        @NotNull
        @Valid
        List<FeedDto> data,

        String nextCursor,

        UUID nextIdAfter,

        @NotNull
        Boolean hasNext,

        @NotNull
        Long totalCount,

        @NotNull
        SortType sortBy,

        @NotNull
        SortDirection direction
) {
}
