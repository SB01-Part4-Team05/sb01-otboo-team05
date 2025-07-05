package com.part4.team05.sb01otbooteam05.domain.clothes.mapper;


import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesDto;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClothesMapper {

  ClothesDto toDto(Clothes clothes);

  Clothes toEntity(ClothesDto clothesDto);

  List<ClothesDto> toDtoList(List<Clothes> list);

}
