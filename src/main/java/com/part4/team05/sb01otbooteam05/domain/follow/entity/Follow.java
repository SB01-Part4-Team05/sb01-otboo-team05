package com.part4.team05.sb01otbooteam05.domain.follow.entity;

import com.part4.team05.sb01otbooteam05.domain.base.BaseEntity;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "follows", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"follower_id", "followee_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow extends BaseEntity {

  @Column(name = "follower_id", nullable = false)
  private UUID follower;

  @Column(name = "followee_id", nullable = false)
  private UUID followee;

  public Follow(UUID follower, UUID followee) {
    this.follower = follower;
    this.followee = followee;
  }
}
