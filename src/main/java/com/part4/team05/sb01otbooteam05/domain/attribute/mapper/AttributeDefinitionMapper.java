package com.part4.team05.sb01otbooteam05.domain.attribute.mapper;

import com.part4.team05.sb01otbooteam05.domain.attribute.dto.ClothesAttributeDefDto;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeDefinition;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AttributeDefinitionMapper {

  ClothesAttributeDefDto toDto(AttributeDefinition attributeDefinition);

  List<ClothesAttributeDefDto> toDtoList(List<AttributeDefinition> defs);

}
