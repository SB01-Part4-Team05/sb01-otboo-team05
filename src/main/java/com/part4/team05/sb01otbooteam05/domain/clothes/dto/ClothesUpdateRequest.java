package com.part4.team05.sb01otbooteam05.domain.clothes.dto;

import com.part4.team05.sb01otbooteam05.domain.attribute.dto.AttributeDto;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeValue;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClothesUpdateRequest {
  String name;
  String type;
  List<AttributeDto> selectableValues;

}
