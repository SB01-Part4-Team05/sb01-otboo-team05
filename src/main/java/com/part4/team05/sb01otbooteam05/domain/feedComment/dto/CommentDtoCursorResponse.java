package com.part4.team05.sb01otbooteam05.domain.feedComment.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.query.SortDirection;

import com.part4.team05.sb01otbooteam05.domain.feed.enums.SortType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CommentDtoCursorResponse(

        @NotNull
        @Valid
        List<CommentDto> data,

        LocalDateTime nextCursor,

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
