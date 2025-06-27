package com.part4.team05.sb01otbooteam05.domain.user.entity;


import com.part4.team05.sb01otbooteam05.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

  // 기본 사용자 정보 (기존 User 테이블)
  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false, length = 20)
  private String name;

  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private UserRole role;

  @Column(nullable = false)
  private boolean locked;

  // 프로필 정보 (NULL 허용) (기존 Profile 테이블)
  @Enumerated(EnumType.STRING)
  @Column(name = "gender")
  private GenderType gender;

  @Column(name = "birth_date")
  private LocalDate birthDate;

  @Column
  private Double latitude;

  @Column
  private Double longitude;

  @Column(name = "location_x")
  private Integer locationX;

  @Column(name = "location_y")
  private Integer locationY;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "location_names", columnDefinition = "jsonb")
  private List<String> locationNames;

  @Column(name = "temperature_sensitivity")
  @Min(value = 0, message = "온도 민감도는 0 이상이어야 합니다") //코드래빗 추천 온도만감도 추가
  @Max(value = 5, message = "온도 민감도는 5 이하여야 합니다")
  private Integer temperatureSensitivity;

  @Column(name = "profile_image_url", length = 500)
  private String profileImageUrl;

  // 임시 비밀번호 관련 (기존 password_reset 테이블)
  @Column(name = "is_temp_password")
  private boolean isTempPassword;

  @Column(name = "password_expires_at")
  private LocalDateTime passwordExpiresAt;

  public static User createUser(String email, String name, String password) {
    return User.builder()
        .email(email)
        .name(name)
        .password(password)
        .role(UserRole.USER)
        .locked(false)
        .isTempPassword(false)
        .build();
  }


}
