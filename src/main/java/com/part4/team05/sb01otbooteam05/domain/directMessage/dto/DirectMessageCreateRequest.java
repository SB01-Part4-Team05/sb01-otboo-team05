package com.part4.team05.sb01otbooteam05.domain.directMessage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DirectMessageCreateRequest(
       @NotNull UUID senderId,
       @NotNull UUID receiverId,
       @NotBlank String content
) {
}
