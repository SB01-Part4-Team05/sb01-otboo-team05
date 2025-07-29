package com.part4.team05.sb01otbooteam05.domain.clothes.mapper;


import com.part4.team05.sb01otbooteam05.domain.attribute.dto.AttributeDto;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeValue;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesDto;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClothesMapper {

  @Mapping(target = "attributes", expression = "java(mapAttributeValues(clothes.getAttributeValues()))")
  @Mapping(target = "type", expression = "java(clothes.getType() != null ? clothes.getType().name() : null)")
  ClothesDto toDto(Clothes clothes);

  @Mapping(target = "attributeValues", ignore = true)
  Clothes toEntity(ClothesDto clothesDto);

  List<ClothesDto> toDtoList(List<Clothes> list);

  default List<AttributeDto> mapAttributeValues(List<AttributeValue> attributeValues) {
    if (attributeValues == null) {
      return java.util.Collections.emptyList();
    }

    return attributeValues.stream()
        .map(AttributeDto::new)
        .collect(Collectors.toList());
  }
}


