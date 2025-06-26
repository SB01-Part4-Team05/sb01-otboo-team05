package com.part4.team05.sb01otbooteam05.domain.ootd.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.part4.team05.sb01otbooteam05.domain.ootd.dto.OotdDto;
import com.part4.team05.sb01otbooteam05.domain.ootd.entity.Ootd;

@Mapper(componentModel = "spring")
public interface OotdMapper {
	OotdDto toDto(Ootd ootd);

	List<OotdDto> toDtoList(List<Ootd> ootds);

	Ootd toEntity(OotdDto dto);

	List<Ootd> toEntityList(List<OotdDto> dtos);


}
