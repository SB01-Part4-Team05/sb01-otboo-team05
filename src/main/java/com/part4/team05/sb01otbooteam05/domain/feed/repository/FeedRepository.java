package com.part4.team05.sb01otbooteam05.domain.feed.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.part4.team05.sb01otbooteam05.domain.feed.entity.Feed;

public interface FeedRepository extends JpaRepository<Feed, UUID> {

}
