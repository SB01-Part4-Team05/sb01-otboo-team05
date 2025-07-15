package com.part4.team05.sb01otbooteam05.domain.user.dto;

import com.part4.team05.sb01otbooteam05.domain.user.entity.UserRole;
import jakarta.validation.constraints.NotNull;

public record UserRoleUpdateRequest(
    @NotNull(message = "권한은 필수입니다")
    UserRole role
) {}
