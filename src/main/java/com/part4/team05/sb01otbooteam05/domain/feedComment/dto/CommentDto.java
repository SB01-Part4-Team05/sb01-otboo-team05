package com.part4.team05.sb01otbooteam05.domain.feedComment.dto;

import com.part4.team05.sb01otbooteam05.domain.feed.dto.AuthorDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record CommentDto(

        @NotNull
        UUID id,

        @NotNull
        LocalDateTime createdAt,

        @NotNull
        UUID feedId,

        @NotNull
        @Valid
        AuthorDto author,

        @NotBlank
        String content
) {
}
