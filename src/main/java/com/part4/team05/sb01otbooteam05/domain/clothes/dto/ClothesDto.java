package com.part4.team05.sb01otbooteam05.domain.clothes.dto;


import com.part4.team05.sb01otbooteam05.domain.attribute.dto.AttributeDto;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClothesDto{
  UUID id;
  String name;
  String imageUrl;
  List<AttributeDto> attributes;
  UUID ownerId;
  String type;
}
