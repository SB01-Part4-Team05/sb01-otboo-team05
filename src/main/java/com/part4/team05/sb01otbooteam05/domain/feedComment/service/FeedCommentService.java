package com.part4.team05.sb01otbooteam05.domain.feedComment.service;

import java.util.UUID;

import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDto;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.CommentCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.FindCommentsRequest;

public interface FeedCommentService {

	CommentDtoCursorResponse findComments(UUID userId, FindCommentsRequest request);

	CommentDto createFeedComment(UUID userId, UUID feedId, CommentCreateRequest request);

}
