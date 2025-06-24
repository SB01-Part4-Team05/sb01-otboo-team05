package com.part4.team05.sb01otbooteam05.domain.attribute.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attribute")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attribute {

  @Id @GeneratedValue(strategy = GenerationType.AUTO)
  UUID id;

  @Column(columnDefinition = "style_type")
  @Enumerated(EnumType.STRING)
  StyleType style;

  @Column(columnDefinition = "color_type")
  @Enumerated(EnumType.STRING)
  ColorType color;

  @Column(columnDefinition = "touch_type")
  @Enumerated(EnumType.STRING)
  TouchType touch;

  @Column(columnDefinition = "thickness_type")
  @Enumerated(EnumType.STRING)
  ThicknessType thickness;
}
