package com.part4.team05.sb01otbooteam05.domain.clothes.entity;

import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeValue;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clothes")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Clothes {

  @Id
  UUID id;

  @Column
  String name;

  @Column(name = "image_url")
  String imageUrl;

  @Column @Enumerated(EnumType.STRING)
  ClothesType type;

  @OneToMany(
      mappedBy = "clothes",
      cascade = CascadeType.ALL,
      fetch = FetchType.EAGER
  )
  private List<AttributeValue> attributeValues;

  @Column(name = "owner_id")
  UUID ownerId;

  public void setAttributeValues(List<AttributeValue> newValues) {
    this.attributeValues.clear();
    for (AttributeValue av : newValues) {
      av.setClothes(this);
      this.attributeValues.add(av);
    }
  }


  public void setName(String name) {
    this.name = name;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public void setType(ClothesType type) {
    this.type = type;
  }

  @PrePersist
  public void ensureId() {
    if (this.id == null) {
      this.id = UUID.randomUUID();
    }
  }
}
