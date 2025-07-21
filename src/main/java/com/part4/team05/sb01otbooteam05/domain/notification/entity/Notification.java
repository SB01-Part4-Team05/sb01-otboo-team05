package com.part4.team05.sb01otbooteam05.domain.notification.entity;

import com.part4.team05.sb01otbooteam05.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

  @Column(length = 100)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private NotificationLevel level;

  @Column
  private boolean isRead = false;

  @Column(nullable = false)
  private UUID receiverId;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  public void markAsRead() {
    this.isRead = true;
  }
}
