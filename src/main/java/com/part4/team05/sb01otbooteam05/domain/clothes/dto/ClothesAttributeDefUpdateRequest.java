package com.part4.team05.sb01otbooteam05.domain.clothes.dto;

import java.util.List;

public record ClothesAttributeDefUpdateRequest(
    String name,
    List<String> selectableValues
) {

}
