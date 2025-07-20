package com.part4.team05.sb01otbooteam05.domain.feed.service;

import java.util.UUID;

import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDto;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FeedCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FeedUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FindFeedsRequest;

public interface FeedService {

    FeedDtoCursorResponse findFeeds(UUID userId, FindFeedsRequest request);

    FeedDto createFeed(UUID userId, FeedCreateRequest request);

    void deleteFeed(UUID userId, UUID feedId);

    FeedDto likeFeed(UUID userId, UUID feedId);

    FeedDto unlikeFeed(UUID userId, UUID feedId);

    FeedDto updateFeed(UUID userId, UUID feedId, FeedUpdateRequest request);
}
