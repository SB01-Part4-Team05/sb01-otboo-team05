package com.part4.team05.sb01otbooteam05.domain.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.List;


public record ProfileUpdateRequest(
    String name,

    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$")
    String gender,

    LocalDate birthDate,

    LocationRequest location,  // 중첩구조

    @Min(value = 0, message = "온도 민감도는 0~5 사이의 값이어야 합니다.")
    @Max(value = 5, message = "온도 민감도는 0~5 사이의 값이어야 합니다.")
    Integer temperatureSensitivity
) {
  public record LocationRequest(
      Double latitude,
      Double longitude,
      Integer x,
      Integer y,
      List<String> locationNames
  ) {}
}
