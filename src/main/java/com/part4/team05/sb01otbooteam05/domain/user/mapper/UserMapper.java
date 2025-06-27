package com.part4.team05.sb01otbooteam05.domain.user.mapper;

import org.mapstruct.Mapper;

import com.part4.team05.sb01otbooteam05.domain.user.dto.AuthorDto;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
	AuthorDto toAuthorDto(User user);

}
