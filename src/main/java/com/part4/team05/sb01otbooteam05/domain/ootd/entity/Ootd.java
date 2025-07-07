package com.part4.team05.sb01otbooteam05.domain.ootd.entity;

import com.part4.team05.sb01otbooteam05.domain.base.BaseEntity;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import com.part4.team05.sb01otbooteam05.domain.feed.entity.Feed;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "ootds")
@NoArgsConstructor
public class Ootd  extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "feed_id")
	private Feed feed;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "clothes_id")
	private Clothes clothes;

	// 다대일 연관관계 동기화 편의메서드
	private Ootd(Feed feed) {
		//todo 생성자 내 검증 로직 추가 필요
		this.feed = feed;
		feed.addOotd(this);
	}
}
