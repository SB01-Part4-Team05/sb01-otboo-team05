package com.part4.team05.sb01otbooteam05.domain.attribute.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attribute_definition")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeDefinition {
  @Id @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column
  private String name;

  @ElementCollection
  private List<String> selectableValues;

  public AttributeDefinition(String n, List<String> list) {
    name = n;
    selectableValues = list;
  }

  public void setSelectableValues(List<String> selectableValues) {
    this.selectableValues = selectableValues;
  }

  public void setName(String name) {
    this.name = name;
  }
}
