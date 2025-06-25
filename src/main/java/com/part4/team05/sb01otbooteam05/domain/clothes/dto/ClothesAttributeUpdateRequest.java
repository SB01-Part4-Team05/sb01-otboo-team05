package com.part4.team05.sb01otbooteam05.domain.clothes.dto;

import java.util.UUID;

public record ClothesAttributeUpdateRequest(
    UUID id,
    UUID definitionId,
    String value
) {

}
