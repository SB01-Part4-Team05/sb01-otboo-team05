package com.part4.team05.sb01otbooteam05.domain.feed.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.part4.team05.sb01otbooteam05.domain.base.BaseEntity;
import com.part4.team05.sb01otbooteam05.domain.ootd.entity.Ootd;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "feeds")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feed extends BaseEntity {

  @Getter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User author;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "weather_id")
  private Weather weather;

  // feed의 ootds 필드를 통해 ootd의 생성, 삭제 등을 모두 관리하므로 ootd 레포지토리는 만들지 않을 예정임.
  // ootd 객체 생성 시 ootds 리스트에도 자동으로 반영된다.
  @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Ootd> ootds = new ArrayList<>();

  @Getter
  @Column(name="content")
  private String content;



  // 생성자
  public Feed(User author, Weather weather, String content) {
    //todo 생성자 내 검증 로직 추가 필요
    this.author = author;
    this.weather = weather;
    this.content = content;
  }

  // 리스트 캡슐화를 위해 읽기전용 리스트를 만들어 반환
  public List<Ootd> getOotds() {
    return Collections.unmodifiableList(ootds);
  }

  // 내용 업데이트 메서드
  public void updateContent(String newContent) {
    if (newContent == null || newContent.trim().isEmpty()) {
      throw new IllegalArgumentException("내용은 null이거나 비어있을 수 없습니다.");
    }
    if (newContent.length() > 1000) {
      throw new IllegalArgumentException("내용은 1000자를 초과할 수 없습니다.");
    }

    this.content = newContent.trim();
  }


  // todo 개발자 오조작 방지 방안 강구
  // Ootd 객체가 만들어질 경우, 자동으로 Feed 객체 내 Ootds 리스트에 자동으로 반영하기 위한 다대일 연관관계 동기화 편의메서드.
  // Ootd 객체를 통한 조작 외 개발자가 직접 Ootd 추가/삭제 메서드를 다루지 않도록 주의.
  public void addOotd(Ootd ootd) {

    // 피드-ootd 간 페어링 오류 검증
    if (this.ootds.contains(ootd)) { // 이 Feed 객체 내 ootds 리스트에 이미 동일한 ootd가 있을 경우
      throw new IllegalArgumentException("해당 피드에 동일한 Ootd가 존재합니다.");
    } else if (!ootd.getFeed().equals(this)) { // Ootd 객체 내 Feed 필드가 이 Feed 객체와 다를 경우
      throw new IllegalArgumentException("해당 피드에 적합한 Ootd 객체가 아닙니다.");
    }

    this.ootds.add(ootd);
  }

  // Ootd 객체 삭제
  public void removeOotd(Ootd ootd) {
    this.ootds.remove(ootd);
  }


}
