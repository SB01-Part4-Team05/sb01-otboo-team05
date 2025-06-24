package com.part4.team05.sb01otbooteam05.domain.user.entity;

import com.part4.team05.sb01otbooteam05.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "password_resets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PasswordReset extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "temporary_password", nullable = false)
  private String temporaryPassword;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
  @Builder.Default
  private Boolean used = false;
}
