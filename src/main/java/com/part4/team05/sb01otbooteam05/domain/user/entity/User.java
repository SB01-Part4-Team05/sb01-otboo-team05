package com.part4.team05.sb01otbooteam05.domain.user.entity;


import com.part4.team05.sb01otbooteam05.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false, length = 20)
  private String name;

  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "user_role DEFAULT 'USER'")
  @Builder.Default
  private UserRole role = UserRole.USER;

  @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
  @Builder.Default
  private Boolean locked = false;

}
