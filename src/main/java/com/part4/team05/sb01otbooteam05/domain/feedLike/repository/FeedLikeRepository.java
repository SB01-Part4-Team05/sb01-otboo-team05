package com.part4.team05.sb01otbooteam05.domain.feedLike.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.part4.team05.sb01otbooteam05.domain.feed.entity.Feed;
import com.part4.team05.sb01otbooteam05.domain.feedLike.entity.FeedLike;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;

public interface FeedLikeRepository extends JpaRepository<FeedLike, UUID> {

	Optional<FeedLike> findByFeedAndAuthor(Feed feed, User author);

	int deleteByFeedAndAuthor(Feed feed, User author);

	void deleteAllByFeed(Feed feed);

	Long countByFeed(Feed feed);

}
