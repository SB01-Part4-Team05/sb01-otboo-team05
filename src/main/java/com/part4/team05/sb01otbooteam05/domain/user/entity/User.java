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
  @Min(value = 0, message = "온도 민감도는 0 이상이어야 합니다") //코드래빗 추천 온도민감도 추가
  @Max(value = 5, message = "온도 민감도는 5 이하여야 합니다")
  private Integer temperatureSensitivity;

  @Column(name = "profile_image_url", length = 500)
  private String profileImageUrl;

  // 임시 비밀번호 관련 (기존 password_reset 테이블)
  @Column(name = "is_temp_password")
  private boolean isTempPassword;

  @Column(name = "password_expires_at")
  private LocalDateTime passwordExpiresAt;

  //회원가입
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

  public void updateName(String name) {
    if (name != null && !name.trim().isEmpty()) {
      this.name = name;
    }
  }

  public void updateGender(GenderType gender) {
    this.gender = gender;
  }

  public void updateBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }

  public void updateLocation(Double latitude, Double longitude, Integer x, Integer y, List<String> locationNames) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.locationX = x;
    this.locationY = y;
    this.locationNames = locationNames;
  }

  public void updateTemperatureSensitivity(Integer temperatureSensitivity) {
    if (temperatureSensitivity != null && temperatureSensitivity >= 0 && temperatureSensitivity <= 5) {
      this.temperatureSensitivity = temperatureSensitivity;
    }
  }

  public void updateProfileImageUrl(String profileImageUrl) {
    this.profileImageUrl = profileImageUrl;
  }

  // 권한 변경
  public void updateRole(UserRole role) {
    if (role != null) {
      this.role = role;
    }
  }

  // 잠금 상태 변경
  public void updateLocked(boolean locked) {
    this.locked = locked;
  }

  //
  public void setTempPassword(String encodedTempPassword, LocalDateTime expireAt) {
    this.password = encodedTempPassword;    // 임시 비번도 해시해서 저장
    this.isTempPassword = true;
    this.passwordExpiresAt = expireAt;
  }

  public void clearTempPassword() {
    this.isTempPassword = false;
    this.passwordExpiresAt = null;
  }

  public boolean isTempPasswordExpired() {
    return isTempPassword && passwordExpiresAt != null
        && passwordExpiresAt.isBefore(LocalDateTime.now());
  }

  public void setPassword(String encodedPassword) {
    this.password = encodedPassword;
  }
}
