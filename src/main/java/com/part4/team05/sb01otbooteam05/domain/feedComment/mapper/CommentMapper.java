package com.part4.team05.sb01otbooteam05.domain.feedComment.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDto;
import com.part4.team05.sb01otbooteam05.domain.feedComment.entity.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {
	CommentDto toDto(Comment comment);

	List<CommentDto> toDtoList(List<Comment> comments);

	Comment toEntity(CommentDto commentDto);

	List<Comment> toEntityList(List<CommentDto> commentDtos);
}
