package com.part4.team05.sb01otbooteam05.domain.feed.entity;

import com.part4.team05.sb01otbooteam05.domain.base.BaseEntity;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import com.part4.team05.sb01otbooteam05.domain.ootd.entity.Ootd;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "feeds")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feed extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User author;

  @ManyToOne
  @JoinColumn(name = "weather_id")
  private Weather weather;

  @OneToMany(mappedBy = "ootd_id")
  private List<Ootd> ootds = new ArrayList<>();

  @Column(name="content")
  private String content;


}
