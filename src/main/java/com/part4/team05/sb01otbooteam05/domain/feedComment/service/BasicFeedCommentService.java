package com.part4.team05.sb01otbooteam05.domain.feedComment.service;

import java.util.UUID;

import com.part4.team05.sb01otbooteam05.domain.notification.entity.NotificationLevel;
import com.part4.team05.sb01otbooteam05.domain.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.part4.team05.sb01otbooteam05.domain.feed.entity.Feed;
import com.part4.team05.sb01otbooteam05.domain.feed.exception.FeedNotFoundException;
import com.part4.team05.sb01otbooteam05.domain.feed.repository.FeedRepository;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDto;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.CommentCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.FindCommentsRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.entity.Comment;
import com.part4.team05.sb01otbooteam05.domain.feedComment.mapper.CommentMapper;
import com.part4.team05.sb01otbooteam05.domain.feedComment.repository.FeedCommentRepository;
import com.part4.team05.sb01otbooteam05.domain.feedComment.repository.SearchCommentRepository;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BasicFeedCommentService implements FeedCommentService {
	private final SearchCommentRepository searchCommentRepository;
	private final FeedRepository feedRepository;
	private final UserService userService;
	private final CommentMapper commentMapper;
	private final FeedCommentRepository feedCommentRepository;
	private final NotificationService notificationService;

	@Override
	@Transactional(readOnly = true)
	public CommentDtoCursorResponse findComments(UUID userId, FindCommentsRequest request) {
		return searchCommentRepository.findCommentDtosWithCursor(userId, request);
	}


	@Override
	public CommentDto createFeedComment(UUID userId, UUID feedId, CommentCreateRequest request) {

		checkUserIdEquality(userId, request.authorId());

		// 1. 피드, 유저 조회
		Feed feed = feedRepository.findById(feedId).orElseThrow(() -> FeedNotFoundException.withId(feedId));
		User author = userService.getUserEntityByIdOrThrow(userId);

		// 2. 댓글 생성
		Comment newComment = new Comment(feed, author, request.content());
		feedCommentRepository.save(newComment);

		// 내 피드에 댓글 등록 알림
		try {
			UUID receiverId = feed.getAuthor().getId();
			if (!receiverId.equals(userId)) {
				notificationService.createAndSendNotification(
						receiverId,
						"댓글 알림",
						author.getName() + "님이 내 피드에 댓글을 달았습니다.",
						NotificationLevel.INFO
				);
			}
		} catch (Exception e) {
			log.warn("댓글 알림 전송 실패: userId={}, feedId={}, commentId={}", userId, feedId, newComment.getId(), e);
		}

		// 3. 댓글 Dto 반환
		log.info("댓글 생성 성공: commentId={}", newComment.getId());
		return commentMapper.toCommentDto(newComment);
	}

	// todo 유저 검증 실패 관련 예외로 변경하기
	// 요청 Id와 파라미터 Id가 같은지 검증 등, ID 동일성 검증용 메서드
	public void checkUserIdEquality(UUID firstId, UUID secondId) {
		if (!firstId.equals(secondId)) {
			throw new IllegalArgumentException();
		}
	}
}
