package com.part4.team05.sb01otbooteam05.domain.clothes.mapper;


import com.part4.team05.sb01otbooteam05.domain.attribute.mapper.AttributeMapper;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesDto;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = AttributeMapper.class)
public interface ClothesMapper {

  @Mapping(target = "attributeDto", source = "attributes")
  ClothesDto toDto(Clothes clothes);

  @Mapping(target = "attributes", source = "attributedDto")
  Clothes toEntity(ClothesDto clothesDto);

}
