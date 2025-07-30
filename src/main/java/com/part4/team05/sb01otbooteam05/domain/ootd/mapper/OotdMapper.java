package com.part4.team05.sb01otbooteam05.domain.ootd.mapper;

import java.util.List;

import com.part4.team05.sb01otbooteam05.domain.attribute.dto.AttributeDto;
import com.part4.team05.sb01otbooteam05.domain.attribute.mapper.AttributeDefinitionMapper;
import lombok.RequiredArgsConstructor;

import com.part4.team05.sb01otbooteam05.domain.ootd.dto.OotdDto;
import com.part4.team05.sb01otbooteam05.domain.ootd.entity.Ootd;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OotdMapper {

	private final AttributeDefinitionMapper attributeDefinitionMapper;

	public OotdDto toOotdDto(Ootd ootd) {
		return new OotdDto(ootd.getClothes().getId(), ootd.getClothes().getName(),
				ootd.getClothes().getImageUrl(), ootd.getClothes().getType().toString(),
				ootd.getClothes().getAttributeValues().stream().map(
						attributeValue -> new AttributeDto(attributeValue)
				).toList());
	}

	public List<OotdDto> toOotdDtoList(List<Ootd> ootds){
		return ootds.stream().map(ootd -> toOotdDto(ootd)).toList();
	}


}
