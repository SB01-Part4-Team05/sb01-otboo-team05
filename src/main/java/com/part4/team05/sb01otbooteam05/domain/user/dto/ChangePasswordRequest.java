package com.part4.team05.sb01otbooteam05.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(@NotBlank(message = "비밀번호를 입력하십시오")
                                    @Size(min = 6, message = "비밀번호는 6자 이상이어야 합니다")
                                    String password
) {}
