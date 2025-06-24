package com.part4.team05.sb01otbooteam05.domain.profile.entity;

import com.part4.team05.sb01otbooteam05.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

  @Id
  @Column(name = "user_id", columnDefinition = "UUID")
  private UUID userId;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "user_id")
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "gender_type")
  private GenderType gender;

  @Column(name = "birth_date")
  private LocalDate birthDate;

  @Column(precision = 10, scale = 8)
  private Double latitude;

  @Column(precision = 11, scale = 8)
  private Double longitude;

  @Column(name = "location_x")
  private Integer locationX;

  @Column(name = "location_y")
  private Integer locationY;

  @Column(name = "location_names")
  private String locationNames;

  @Column(name = "temperature_sensitivity")
  private Integer temperatureSensitivity;

  @Column(name = "profile_image_url", length = 500)
  private String profileImageUrl;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

}
