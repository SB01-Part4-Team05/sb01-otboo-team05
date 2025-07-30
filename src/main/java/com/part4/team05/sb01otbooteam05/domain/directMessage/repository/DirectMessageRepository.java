package com.part4.team05.sb01otbooteam05.domain.directMessage.repository;

import com.part4.team05.sb01otbooteam05.domain.directMessage.entity.DirectMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID> {

    @Query("""
        SELECT m 
        FROM DirectMessage m 
        WHERE (m.sender.id = :userId OR m.receiver.id = :userId)
          AND (:idAfter IS NULL OR m.id < :idAfter)
        ORDER BY m.id DESC
    """)
    List<DirectMessage> findByUserIdAndIdLessThan(
            @Param("userId") UUID userId,
            @Param("idAfter") UUID idAfter,
            Pageable pageable
    );

    @Query("""
        SELECT COUNT(m) 
        FROM DirectMessage m 
        WHERE m.sender.id = :userId OR m.receiver.id = :userId
    """)
    long countByUserId(@Param("userId") UUID userId);
}
