package com.part4.team05.sb01otbooteam05.domain.clothes.dto;

import com.part4.team05.sb01otbooteam05.domain.attribute.dto.AttributeDto;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeValue;
import java.util.List;
import java.util.UUID;

public record ClothesUpdateRequest(
    String name,
    String type,
    List<AttributeDto> selectableValues
) {

}
