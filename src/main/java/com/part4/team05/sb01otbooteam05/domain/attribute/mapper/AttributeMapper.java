package com.part4.team05.sb01otbooteam05.domain.attribute.mapper;

import com.part4.team05.sb01otbooteam05.domain.attribute.dto.AttributeDto;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.Attribute;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface AttributeMapper {
  AttributeDto toDto(Attribute attribute);
  Attribute toEntity(AttributeDto attributeDto);

}
