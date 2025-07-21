package com.part4.team05.sb01otbooteam05.domain.feed.repository;

import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FindFeedsRequest;

import java.util.UUID;

public interface SearchFeedRepository {
    FeedDtoCursorResponse findFeedDtosWithCursor(UUID userId, FindFeedsRequest request);

}
