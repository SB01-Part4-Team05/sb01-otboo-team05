package com.part4.team05.sb01otbooteam05.domain.feedComment.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.part4.team05.sb01otbooteam05.domain.feed.entity.Feed;
import com.part4.team05.sb01otbooteam05.domain.feed.exception.FeedNotFoundException;
import com.part4.team05.sb01otbooteam05.domain.feed.mapper.FeedMapper;
import com.part4.team05.sb01otbooteam05.domain.feed.repository.FeedRepository;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDto;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.CommentCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.FindCommentsRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.entity.Comment;
import com.part4.team05.sb01otbooteam05.domain.feedComment.mapper.CommentMapper;
import com.part4.team05.sb01otbooteam05.domain.feedComment.repository.FeedCommentRepository;
import com.part4.team05.sb01otbooteam05.domain.feedComment.repository.SearchCommentRepository;
import com.part4.team05.sb01otbooteam05.domain.notification.entity.NotificationLevel;
import com.part4.team05.sb01otbooteam05.domain.notification.entity.NotificationType;
import com.part4.team05.sb01otbooteam05.domain.notification.service.NotificationService;
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
	//private final ApplicationEventPublisher eventPublisher;
	private final FeedMapper feedMapper;

	@Override
	@Transactional(readOnly = true)
	public CommentDtoCursorResponse findComments(UUID userId, FindCommentsRequest request) {
		return searchCommentRepository.findCommentDtosWithCursor(userId, request);
	}

	@Override
	@Transactional
	public CommentDto createFeedComment(UUID userId, UUID feedId, CommentCreateRequest request) {

		checkUserIdEquality(userId, request.authorId());

		// 1. 피드, 유저 조회
		Feed feed = feedRepository.findById(feedId).orElseThrow(() -> FeedNotFoundException.withId(feedId));
		User commentAuthor = userService.getUserEntityByIdOrThrow(userId);

		// 2. 댓글 생성
		Comment newComment = new Comment(feed, commentAuthor, request.content());
		feedCommentRepository.save(newComment);


		/* 댓글 작성 알림 이벤트 발행 이었던 것. . . .
		 	이벤트발행 디져도안돼서 그냥 조금 촌스럽지만 호출하도록하겠습니다 ^^..;
		 	추후 여유가된다면 리팩토링하긔... ㅠㅠ
			eventPublisher.publishEvent(new CommentCreatedEvent(feedId, feedMapper.toAuthorDto(commentAuthor), request.content()));
		*/ //todo 이벤트리스터로 변경
		// 3. 댓글 작성 알림 발송
		notificationService.sendNotification(
			commentAuthor.getId(),
				commentAuthor.getName() + "님이 댓글을 달았어요.",
			newComment.getContent(),
			feed.getId(),
			NotificationType.RECEIVED_COMMENT,
			NotificationLevel.INFO
		);

		// 4. 댓글 Dto 반환
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
