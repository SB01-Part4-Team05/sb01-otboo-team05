package com.part4.team05.sb01otbooteam05.domain.directMessage.repository;

import com.part4.team05.sb01otbooteam05.domain.directMessage.entity.DirectMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID> {

    @Query("""
        SELECT m FROM DirectMessage m
        WHERE 
            ((m.sender.id = :userId1 AND m.receiver.id = :userId2) OR
             (m.sender.id = :userId2 AND m.receiver.id = :userId1))
            AND (:idAfter IS NULL OR m.id < :idAfter)
        ORDER BY m.id DESC
        """)
    List<DirectMessage> findMessages(
            UUID userId1,
            UUID userId2,
            UUID idAfter,
            Pageable pageable
    );

    @Query("""
    SELECT COUNT(m) FROM DirectMessage m
    WHERE 
        (m.sender.id = :userId1 AND m.receiver.id = :userId2)
        OR (m.sender.id = :userId2 AND m.receiver.id = :userId1)
""")
    long countByUserPair(UUID userId1, UUID userId2);
}
