package com.part4.team05.sb01otbooteam05.domain.ootd.dto;

import java.util.List;
import java.util.UUID;

import com.part4.team05.sb01otbooteam05.domain.attribute.dto.AttributeDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OotdDto(


	@NotNull
	UUID clothesID,


	@NotBlank
	String name,

	String imageUrl,

	@NotNull
	String type,

	@NotNull
	@Valid
	List<AttributeDto> attributes
) {
}
