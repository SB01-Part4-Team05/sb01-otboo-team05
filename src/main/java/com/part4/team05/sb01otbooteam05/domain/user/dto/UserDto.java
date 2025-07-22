package com.part4.team05.sb01otbooteam05.domain.user.dto;

import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

  private UUID id;
  private String email;
  private String name;
  private String role; //스웨거 참고해보니 role이 string
  private boolean locked;
  private LocalDateTime createdAt;
  private List<String> linkedOAuthProviders;


  public static UserDto from(User user) {
    List<String> providers = (user.getProvider() != null && !user.getProvider().equalsIgnoreCase("LOCAL"))
        ? Collections.singletonList(user.getProvider().toLowerCase())
        : Collections.emptyList();

    return UserDto.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .role(user.getRole().name())  // enum을 String으로 변환
        .locked(user.isLocked())
        .createdAt(user.getCreatedAt())
        .linkedOAuthProviders(providers)
        .build();
  }
}
