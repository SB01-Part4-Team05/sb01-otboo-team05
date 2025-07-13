package com.part4.team05.sb01otbooteam05.domain.feedComment.mapper;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.part4.team05.sb01otbooteam05.domain.feed.dto.AuthorDto;
import com.part4.team05.sb01otbooteam05.domain.feed.mapper.FeedMapper;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDto;
import com.part4.team05.sb01otbooteam05.domain.feedComment.entity.Comment;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentMapper {

	private final FeedMapper feedMapper;

	List<CommentDto> toCommentDtoList(List<Comment> comments){
		return comments.stream().map(this::toCommentDto).toList();
	}

	AuthorDto toAuthorDto(User user){
		return feedMapper.toAuthorDto(user);
	}

	public CommentDto toCommentDto(Comment comment) {
		return new CommentDto(
			comment.getId(),
			comment.getCreatedAt(),
			comment.getFeed().getId(),
			toAuthorDto(comment.getAuthor()),
			comment.getContent()
		);
	}
}
