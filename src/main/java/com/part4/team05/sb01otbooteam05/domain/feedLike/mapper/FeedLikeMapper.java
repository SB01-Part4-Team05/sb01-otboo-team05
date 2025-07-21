package com.part4.team05.sb01otbooteam05.domain.feedLike.mapper;


import com.part4.team05.sb01otbooteam05.domain.feed.mapper.FeedMapper;
import com.part4.team05.sb01otbooteam05.domain.feedLike.dto.FeedLikeDto;
import com.part4.team05.sb01otbooteam05.domain.feedLike.entity.FeedLike;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedLikeMapper {

    private final FeedMapper feedMapper;

    public FeedLikeDto toDto(FeedLike feedLike, User user) {
        return new FeedLikeDto(feedMapper.toFeedDto(feedLike.getFeed(), user), feedMapper.toAuthorDto(feedLike.getAuthor()));
    }

}
