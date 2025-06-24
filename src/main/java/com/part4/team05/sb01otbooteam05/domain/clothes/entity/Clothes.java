package com.part4.team05.sb01otbooteam05.domain.clothes.entity;

import com.part4.team05.sb01otbooteam05.domain.attribute.entity.Attribute;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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

  @Id @GeneratedValue(strategy = GenerationType.AUTO)
  UUID id;

  @Column
  String name;

  @Column(name = "image_url")
  String imageUrl;

  @Column @Enumerated(EnumType.STRING)
  ClothesType type;

  @OneToMany(mappedBy = "clothes", cascade = CascadeType.ALL)
  private List<Attribute> attributes;

  @Column(name = "owner_id")
  UUID ownerId;

  public void setAttributes(
      List<Attribute> attributes) {
    this.attributes = attributes;
  }
}
