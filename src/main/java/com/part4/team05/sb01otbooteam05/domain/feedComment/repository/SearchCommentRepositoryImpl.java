package com.part4.team05.sb01otbooteam05.domain.feedComment.repository;


import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDto;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.feed.entity.Feed;
import com.part4.team05.sb01otbooteam05.domain.feed.entity.QFeed;
import com.part4.team05.sb01otbooteam05.domain.feed.enums.SortType;
import com.part4.team05.sb01otbooteam05.domain.feed.mapper.FeedMapper;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.FindCommentsRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.entity.QComment;
import com.part4.team05.sb01otbooteam05.domain.user.entity.QUser;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.exception.UserNotFoundException;
import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.QWeather;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.SortDirection;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SearchCommentRepositoryImpl implements SearchCommentRepository {
    private final JPAQueryFactory queryFactory;
    private final FeedMapper feedMapper;
    private final UserRepository userRepository;
    private final QFeed feed = QFeed.feed;
    private final QUser author = QUser.user;
    private final QWeather weather = QWeather.weather;
    private final QComment comment = QComment.comment;

    @Override
    public FeedDtoCursorResponse findCommentDtosWithCursor(UUID userId, FindCommentsRequest request) {

        BooleanBuilder builder = new BooleanBuilder();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());


        // 정렬 방식 설정
        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(request.sortBy(), request.sortDirection());

        List<Feed> feedEntityList = queryFactory
                .selectFrom(feed)
                .leftJoin(feed.author, author).fetchJoin()
                .leftJoin(feed.weather, weather).fetchJoin()
                .where(builder)
                .orderBy(orderSpecifier)
                .limit(request.limit() + 1)
                .fetch();

        Boolean hasNext = (feedEntityList.size() > request.limit());
        String nextCursor = null;
        String nextIdAfter = null;
        SortDirection nextDirection = request.sortDirection();
        String nextSortBy = request.sortBy().toString();

        // 해당 검색 조건으로 조회한 총 데이터 개수 반환
        Long totalCount = queryFactory
                .select(feed.count())
                .from(feed)
                .where(builder)
                .fetchOne();

        // 다음 페이지가 있다면 커서와 IdAfter 반환하고, 추가로 불러온 데이터 제거
        if (hasNext) {
            Feed lastFeed = feedEntityList.get(request.limit());
            nextCursor = (request.sortBy() == SortType.createdAt) ? lastFeed.getCreatedAt().toString() : lastFeed.getLikeCount().toString();
            feedEntityList.remove(request.limit());
        }

        // hasNext 판별을 위한 추가 데이터가 있다면 제거하고, Dto 변환
        List<FeedDto> data = feedMapper.toFeedDtoList(feedEntityList, user);
        return new FeedDtoCursorResponse(data, nextCursor, nextIdAfter, hasNext, totalCount, nextSortBy, nextDirection);
    }

}

