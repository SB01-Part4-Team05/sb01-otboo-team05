package com.part4.team05.sb01otbooteam05.domain.directMessage.entity;

import com.part4.team05.sb01otbooteam05.domain.base.BaseEntity;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import jakarta.persistence.*;

@Entity
@Table(name = "direct_messages")
public class DirectMessage extends BaseEntity {

    @Lob
    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;
}
