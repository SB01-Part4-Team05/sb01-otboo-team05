package com.part4.team05.sb01otbooteam05.domain.weather.entity;

import com.part4.team05.sb01otbooteam05.domain.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@AllArgsConstructor
@Table(name = "weathers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Weather extends BaseEntity {


}
