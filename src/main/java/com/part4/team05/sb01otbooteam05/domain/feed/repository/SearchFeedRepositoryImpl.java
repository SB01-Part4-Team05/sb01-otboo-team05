package com.part4.team05.sb01otbooteam05.domain.feed.repository;


import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDto;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FindFeedsRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.entity.Feed;
import com.part4.team05.sb01otbooteam05.domain.feed.entity.QFeed;
import com.part4.team05.sb01otbooteam05.domain.feed.enums.SortType;
import com.part4.team05.sb01otbooteam05.domain.feed.mapper.FeedMapper;
import com.part4.team05.sb01otbooteam05.domain.user.entity.QUser;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.exception.UserNotFoundException;
import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.QWeather;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.SortDirection;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SearchFeedRepositoryImpl implements SearchFeedRepository{
    private final JPAQueryFactory queryFactory;
    private final FeedMapper feedMapper;
    private final UserRepository userRepository;
    private final QFeed feed = QFeed.feed;
    private final QUser author = QUser.user;
    private final QWeather weather = QWeather.weather;

    @Override
    public FeedDtoCursorResponse findFeedDtosWithCursor(UUID userId, FindFeedsRequest request) {

        BooleanBuilder builder = new BooleanBuilder();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());


        // 글 내용 조건 추가
        if (!request.keywordLike().isEmpty()) {
            builder.and(feed.content.containsIgnoreCase(request.keywordLike()));
        }

        // 날씨 조건 추가
        if (request.skyStatusEqual() != null) {
            builder.and(feed.weather.skyStatusType.eq(request.skyStatusEqual()));
        }

        // 강수 조건 추가
        if (request.precipitationTypeEqual() != null) {
            builder.and(feed.weather.precipitationType.eq(request.precipitationTypeEqual()));
        }

        // 작성자 조건 추가
        if (request.authorIdEqual() != null) {
            builder.and(feed.author.id.eq(request.authorIdEqual()));
        }

        // 커서 기반 페이지네이션 조건 추가
        if (!request.cursor().isEmpty()) {
            LocalDateTime cursorTime = LocalDateTime.parse(request.cursor());

            if (request.sortDirection() == SortDirection.DESCENDING) {
                builder.and(
                        feed.createdAt.lt(cursorTime)
                                .or(feed.createdAt.eq(cursorTime).and(feed.id.lt(request.idAfter())))
                );
            } else {
                builder.and(
                        feed.createdAt.gt(cursorTime)
                                .or(feed.createdAt.eq(cursorTime).and(feed.id.gt(request.idAfter())))
                );
            }
        }

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

    // 정렬방식 지정 메서드
    private OrderSpecifier<?> getOrderSpecifier (SortType sortType, SortDirection direction){
        Order order = (direction == SortDirection.DESCENDING) ? Order.DESC : Order.ASC;

        // 정렬할 기준필드 설정
        return switch (sortType) {
            case createdAt -> new OrderSpecifier<>(order, feed.createdAt);
            case likeCount -> new OrderSpecifier<>(order, feed.likeCount);
            default -> new OrderSpecifier<>(order, feed.createdAt); // 기본값
        };
    }
}

