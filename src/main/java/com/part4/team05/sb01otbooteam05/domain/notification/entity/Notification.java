package com.part4.team05.sb01otbooteam05.domain.notification.entity;

import com.part4.team05.sb01otbooteam05.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private NotificationType type;

  private UUID entityId;

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
}
