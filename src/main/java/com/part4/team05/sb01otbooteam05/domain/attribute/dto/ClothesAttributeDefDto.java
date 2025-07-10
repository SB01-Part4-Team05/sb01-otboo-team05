package com.part4.team05.sb01otbooteam05.domain.attribute.dto;

import java.util.List;
import java.util.UUID;

public record ClothesAttributeDefDto(
    UUID id,
    String name,
    List<String> selectableValues
) {

}
