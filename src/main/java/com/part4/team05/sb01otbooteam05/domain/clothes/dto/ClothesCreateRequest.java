package com.part4.team05.sb01otbooteam05.domain.clothes.dto;

import com.part4.team05.sb01otbooteam05.domain.attribute.entity.Attribute;
import java.util.List;
import java.util.UUID;

public record ClothesCreateRequest(
    UUID ownerId,
    String name,
    String type,
    List<ClothesAttributeDto> attributes
) {

}
