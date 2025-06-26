package com.part4.team05.sb01otbooteam05.domain.attribute.entity;

import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
public class AttributeValue {

  @Id @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String value;

  @ManyToOne
  @JoinColumn(name = "clothes_id")
  private Clothes clothes;

  @ManyToOne
  @JoinColumn(name = "definition_id")
  private AttributeDefinition definition;

  public void setValue(String value) {
    this.value = value;
  }
}
