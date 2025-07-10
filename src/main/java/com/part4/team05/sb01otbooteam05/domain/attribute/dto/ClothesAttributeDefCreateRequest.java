package com.part4.team05.sb01otbooteam05.domain.attribute.dto;

import java.util.List;

public record ClothesAttributeDefCreateRequest(
    String name,
    List<String> selectableValues
) {

}
