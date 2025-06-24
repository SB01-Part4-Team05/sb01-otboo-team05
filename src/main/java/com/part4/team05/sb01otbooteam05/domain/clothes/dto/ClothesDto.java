package com.part4.team05.sb01otbooteam05.domain.clothes.dto;


import java.util.UUID;

public record ClothesDto(
    UUID id,
    String name,
    String imageUrl,
    AttributeDto attributeDto,
    UserDto userDto
) {
}
