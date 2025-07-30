package com.part4.team05.sb01otbooteam05.domain.recommend.dto;

import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesDto;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record RecommendationDto(
    UUID weatherId,
    UUID userId,
    List<ClothesDto> clothes
) {

}
