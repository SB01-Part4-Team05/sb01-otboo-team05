package com.part4.team05.sb01otbooteam05.domain.feed.entity;

import com.part4.team05.sb01otbooteam05.domain.base.BaseEntity;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import com.part4.team05.sb01otbooteam05.domain.ootd.entity.Ootd;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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



  public Feed(User author, Weather weather, String content) {
    //todo 생성자 내 검증 로직 추가 필요
    this.author = author;
    this.weather = weather;
    this.content = content;
  }

  public void addOotd(Ootd ootd) {
    this.ootds.add(ootd);
    if (!this.ootds.contains(this)) {
      ootds.add(ootd);
    }
  }

  public void removeOotd(Ootd ootd) {
    this.ootds.remove(ootd);
  }

  // 리스트 캡슐화를 위해 읽기전용 리스트를 만들어 반환
  public List<Ootd> getOotds() {
    return Collections.unmodifiableList(ootds);
  }
}
