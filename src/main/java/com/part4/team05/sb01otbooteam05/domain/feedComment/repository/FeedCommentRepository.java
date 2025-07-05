package com.part4.team05.sb01otbooteam05.domain.feedComment.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.part4.team05.sb01otbooteam05.domain.feed.entity.Feed;
import com.part4.team05.sb01otbooteam05.domain.feedComment.entity.Comment;

public interface FeedCommentRepository extends JpaRepository<Comment, UUID> {

	Integer countByFeed(Feed feed);

	void deleteAllByFeed(Feed feed);
}
