package com.part4.team05.sb01otbooteam05.domain.clothes.entity;

import com.part4.team05.sb01otbooteam05.domain.attribute.entity.Attribute;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
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
import java.util.UUID;

@Entity
@Table(name = "clothes")
public class Clothes {

  @Id @GeneratedValue(strategy = GenerationType.AUTO)
  UUID id;

  @Column
  String name;

  @Column(name = "image_url")
  String imageUrl;

  @Column @Enumerated(EnumType.STRING)
  ClothesType type;

  @JoinColumn(name = "attrebutes")
  Attribute attributes;

  @OneToOne
  @JoinColumn(name = "owner_id")
  User user;
}
