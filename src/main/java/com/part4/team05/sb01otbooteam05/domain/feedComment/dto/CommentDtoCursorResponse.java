package com.part4.team05.sb01otbooteam05.domain.feedComment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.hibernate.query.SortDirection;

import java.util.List;

public record CommentDtoCursorResponse(

        @NotNull
        @Valid
        List<CommentDto> data,

        String nextCursor,

        String nextIdAfter,

        @NotNull
        Boolean hasNext,

        @NotNull
        Long totalCount,

        @NotNull
        String sortBy,

        @NotNull
        SortDirection direction
) {
}
