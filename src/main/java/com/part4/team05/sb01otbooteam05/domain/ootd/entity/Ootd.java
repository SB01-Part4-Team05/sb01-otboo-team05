package com.part4.team05.sb01otbooteam05.domain.ootd.entity;

import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import com.part4.team05.sb01otbooteam05.domain.feed.entity.Feed;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "ootds")
@NoArgsConstructor
public class Ootd {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id")
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clothes_id")
    private Clothes clothes;


    public Ootd(Feed feed, Clothes clothes) {
        //todo 생성자 내 검증 로직 추가 필요
        this.feed = feed;
        this.clothes = clothes;

        // 다대일 연관관계 동기화
        feed.addOotd(this);
    }
}
