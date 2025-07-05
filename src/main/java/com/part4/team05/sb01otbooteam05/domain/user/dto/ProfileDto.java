package com.part4.team05.sb01otbooteam05.domain.user.dto;

import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileDto {  // 외부 클래스

  private UUID userId;
  private String name;
  private String gender;
  private LocalDate birthDate;
  private LocationDto location;  // 스웨거 참고시 중첩구조
  private Integer temperatureSensitivity;
  private String profileImageUrl;

  // ProfileDto 안에 있는 내부 클래스
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class LocationDto {  // 내부 클래스
    private Double latitude;
    private Double longitude;
    private Integer x;
    private Integer y;
    private List<String> locationNames;
  }

  public static ProfileDto from(User user) {
    LocationDto location = null;
    if (user.getLatitude() != null && user.getLongitude() != null) {
      location = LocationDto.builder()
          .latitude(user.getLatitude())
          .longitude(user.getLongitude())
          .x(user.getLocationX())
          .y(user.getLocationY())
          .locationNames(user.getLocationNames())
          .build();
    }

    return ProfileDto.builder()
        .userId(user.getId())
        .name(user.getName())
        .gender(user.getGender() != null ? user.getGender().name() : null)
        .birthDate(user.getBirthDate())
        .location(location)
        .temperatureSensitivity(user.getTemperatureSensitivity())
        .profileImageUrl(user.getProfileImageUrl())
        .build();
  }
}
