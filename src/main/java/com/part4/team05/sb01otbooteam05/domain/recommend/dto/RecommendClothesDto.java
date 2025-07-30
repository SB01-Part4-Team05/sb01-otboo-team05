package com.part4.team05.sb01otbooteam05.domain.recommend.dto;

import com.part4.team05.sb01otbooteam05.domain.attribute.dto.AttributeDto;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesDto;
import java.util.List;
import java.util.UUID;

public record RecommendClothesDto(
    UUID clothesId,
    String name,
    String imageUrl,
    String type,
    List<AttributeDto> attributes
) {
  public static RecommendClothesDto from(ClothesDto clothesDto) {
    return new RecommendClothesDto(
        clothesDto.getId(),
        clothesDto.getName(),
        clothesDto.getImageUrl(),
        clothesDto.getType(),
        clothesDto.getAttributes()
    );
  }
}
