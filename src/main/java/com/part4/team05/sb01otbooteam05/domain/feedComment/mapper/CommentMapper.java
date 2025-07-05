package com.part4.team05.sb01otbooteam05.domain.feedComment.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.part4.team05.sb01otbooteam05.domain.feed.dto.AuthorDto;
import com.part4.team05.sb01otbooteam05.domain.feed.mapper.FeedMapper;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDto;
import com.part4.team05.sb01otbooteam05.domain.feedComment.entity.Comment;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;

@Mapper(componentModel = "spring", uses = FeedMapper.class)
public interface CommentMapper {

	List<CommentDto> toDtoList(List<Comment> comments);

	AuthorDto toAuthorDto(User user);

	default CommentDto toDto(Comment comment) {
		return new CommentDto(
			comment.getId(),
			comment.getCreatedAt(),
			comment.getFeed().getId(),
			toAuthorDto(comment.getAuthor()),
			comment.getContent()
		);
	}
}
