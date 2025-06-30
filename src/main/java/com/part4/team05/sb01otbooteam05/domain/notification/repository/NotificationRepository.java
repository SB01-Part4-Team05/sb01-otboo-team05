package com.part4.team05.sb01otbooteam05.domain.notification.repository;

import com.part4.team05.sb01otbooteam05.domain.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("SELECT n FROM Notification n " +
            "WHERE n.receiverId = :userId " +
            "AND (:idAfter IS NULL OR n.id < :idAfter) " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findNotifications(UUID userId, UUID idAfter, Pageable pageable);

    long countByReceiverId(UUID receiverId);
}
