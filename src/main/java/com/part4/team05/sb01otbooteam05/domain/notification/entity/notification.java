package com.part4.team05.sb01otbooteam05.domain.notification.entity;

import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class notification {

  @Id
  @Column(nullable = false)
  private UUID id;

  @Column(nullable = false, length = 30)
  private String type;

  private UUID entityId;

  @Column(length = 100)
  private String title;

  @Lob
  private String content;

  @Column(length = 20)
  private String level;

  @Column(nullable = false)
  private boolean isRead = false;

  @Column(nullable = false)
  private Timestamp createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_id", nullable = false)
  private User receiver;
}
