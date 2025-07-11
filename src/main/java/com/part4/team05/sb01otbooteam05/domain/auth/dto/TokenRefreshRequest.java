package com.part4.team05.sb01otbooteam05.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(
    @NotBlank(message = "리프레시 토큰은 필수입니다")
    String refreshToken
) {}
