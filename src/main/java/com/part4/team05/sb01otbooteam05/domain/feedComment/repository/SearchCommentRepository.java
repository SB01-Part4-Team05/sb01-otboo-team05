package com.part4.team05.sb01otbooteam05.domain.feedComment.repository;

import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.FindCommentsRequest;

import java.util.UUID;

public interface SearchCommentRepository {
    CommentDtoCursorResponse findCommentDtosWithCursor(UUID userId, FindCommentsRequest request);

}
