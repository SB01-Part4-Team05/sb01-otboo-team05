package com.part4.team05.sb01otbooteam05.domain.feedComment.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import com.part4.team05.sb01otbooteam05.domain.feed.dto.AuthorDto;
import com.part4.team05.sb01otbooteam05.domain.feed.entity.Feed;
import com.part4.team05.sb01otbooteam05.domain.feed.exception.FeedNotFoundException;
import com.part4.team05.sb01otbooteam05.domain.feed.repository.FeedRepository;
import com.part4.team05.sb01otbooteam05.domain.notification.entity.NotificationLevel;
import com.part4.team05.sb01otbooteam05.domain.notification.entity.NotificationType;
import com.part4.team05.sb01otbooteam05.domain.notification.service.NotificationService;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@RequiredArgsConstructor
public class CommentCreatedEventListener {
	private final NotificationService notificationService;
	private final FeedRepository feedRepository;

	// 댓글 생성 시 이벤트 처리
	@Async
	@EventListener
	public void handleCommentCreated(CommentCreatedEvent event) {
		log.info("CommentCreatedEvent 리스너 수신 : 알림 내용={}", event.getContent());
		Feed feed = feedRepository.findById(event.getFeedId()).orElseThrow(() -> new FeedNotFoundException());
		User feedAuthor = feed.getAuthor();
		AuthorDto commentAuthor = event.getAuthor();

		// 피드 작성자 본인이 작성한 댓글이면 알림 보내지 않음.
		if (!feedAuthor.getId().equals(commentAuthor.id())) {
			// 알림 발송
			notificationService.sendNotification(
				feedAuthor.getId(),
				commentAuthor.name()+"님이 댓글을 달았어요.",
				event.getContent(),
				feed.getId(),
				NotificationType.RECEIVED_COMMENT,
				NotificationLevel.INFO
			);
		}
	}
}
