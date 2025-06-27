package com.part4.team05.sb01otbooteam05.domain.ootd.dto;

import java.util.List;
import java.util.UUID;

import com.part4.team05.sb01otbooteam05.domain.attribute.dto.AttributeDto;

public record OotdDto(
	UUID clothesID,
	String name,
	String imageUrl,
	String type,
	List<AttributeDto> attributes
) {
}
