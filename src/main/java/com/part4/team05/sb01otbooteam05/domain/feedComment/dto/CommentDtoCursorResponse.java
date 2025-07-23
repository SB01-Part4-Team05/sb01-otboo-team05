package com.part4.team05.sb01otbooteam05.domain.feedComment.dto;

import com.part4.team05.sb01otbooteam05.domain.feed.enums.SortType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.hibernate.query.SortDirection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
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
