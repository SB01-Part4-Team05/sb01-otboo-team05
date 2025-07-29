package com.part4.team05.sb01otbooteam05.domain.recommend.dto;

import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesDto;
import java.util.List;
import java.util.UUID;

public record RecommendationiDto(
    UUID weatherId,
    UUID userId,
    List<List<ClothesDto>> clothes
) {

}
