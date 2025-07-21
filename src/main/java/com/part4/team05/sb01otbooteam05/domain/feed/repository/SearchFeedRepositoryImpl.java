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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SearchFeedRepositoryImpl implements SearchFeedRepository {
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

        // 커서가 존재할 경우 커서 기반 페이지네이션 조건 추가
        if (!(request.cursor().isEmpty()) && (request.idAfter() != null)) {
            // 1. 기본값인 최신순 or 좋아요순일 경우, 내림차순(DESCENDING)으로 설정.
            if (request.sortDirection() == SortDirection.DESCENDING) {
                // 1-1. 최신순인 경우(정렬기준이 생성일)
                if (request.sortBy().equals(SortType.createdAt)) {
                    LocalDateTime cursorTime = LocalDateTime.parse(request.cursor());
                    builder.and(
                            feed.createdAt.lt(cursorTime) // 1-1-1. 커서 시간 미만의 데이터만 필터링
                                    .or(feed.createdAt.eq(cursorTime)
                                            .and(feed.id.lt(request.idAfter()))) // 1-1-2. 커서시간 동일할 시 ID로 필터링
                    );
                } else {
                    // 1-2. 좋아요순인 경우(정렬기준이 좋아요수)
                    long cursorLikeCount = Long.parseLong(request.cursor());
                    builder.and(
                            feed.likeCount.lt(cursorLikeCount) // 1-2-1. 커서 좋아요수 미만의 데이터만 필터링
                                    .or(feed.likeCount.eq(cursorLikeCount)
                                            .and(feed.id.lt(request.idAfter()))) // 1-2-2. 커서 좋아요수 동일할 시 ID로 필터링
                    );
                }
                // 2. ASENDING으로 설정되어 있을 경우, 오름차순으로 설정.
            } else {
                // 2-1. 오래된순인 경우(정렬기준이 생성일)
                if (request.sortBy().equals(SortType.createdAt)) {

                    LocalDateTime cursorTime = LocalDateTime.parse(request.cursor());
                    builder.and(
                            feed.createdAt.gt(cursorTime) // 2-1-1. 커서 시간 초과의 데이터만 필터링
                                    .or(feed.createdAt.eq(cursorTime)
                                            .and(feed.id.gt(request.idAfter()))) // 2-1-2. 커서시간 동일할 시 ID로 필터링
                    );
                    // 2-2. 좋아요 낮은순인 경우(정렬기준이 좋아요수)
                } else {
                    long cursorLikeCount = Long.parseLong(request.cursor());
                    builder.and(
                            feed.likeCount.gt(cursorLikeCount) // 2-2-1. 커서 좋아요수 초과의 데이터만 필터링
                                    .or(feed.likeCount.eq(cursorLikeCount)
                                            .and(feed.id.gt(request.idAfter()))) // 2-2-2. 커서 좋아요수 동일할 시 ID로 필터링
                    );
                }
            }
        }
        // todo 여유있다면 redis로 총개수 캐싱
        // 해당 검색 조건으로 조회한 총 데이터 개수 반환
        Long totalCount = queryFactory
                .select(feed.count())
                .from(feed)
                .where(builder)
                .fetchOne();

        // 정렬 방식 설정
        OrderSpecifier<?>[] orderSpecifiers = getOrderSpecifiers(request.sortBy(), request.sortDirection());
        // 조회
        List<Feed> feedEntityList = queryFactory
                .selectFrom(feed)
                .leftJoin(feed.author, author).fetchJoin()
                .leftJoin(feed.weather, weather).fetchJoin()
                .where(builder)
                .orderBy(orderSpecifiers)
                .limit(request.limit() + 1)
                .fetch();

        // limit+1개가 조회되었는지를 확인하여 다음 페이지가 있는지 여부 설정.
        Boolean hasNext = (feedEntityList.size() > request.limit());
        // 다음 페이지가 있다면 추가로 불러온 데이터 제거
        if (hasNext) {
            feedEntityList.remove(request.limit());
        }
        // 검색된 피드가 없다면 빈 데이터 반환
        if (feedEntityList.isEmpty()) {
            return new FeedDtoCursorResponse(Collections.emptyList(), null, null, false, totalCount, request.sortBy(), request.sortDirection());
        }

        // 정렬방향과 정렬필드는 이번 조회에 사용됐던 값을 그대로 반환한다.
        SortDirection nextDirection = request.sortDirection();
        SortType nextSortBy = request.sortBy();

        // 검색값의 마지막 객체가 커서로서 사용된다.
        Feed cursorEntity = feedEntityList.get(feedEntityList.size() - 1);
        // 정렬필드에 따라 생성일 or 좋아요 수가 커서가 된다.
        String nextCursor = (request.sortBy() == SortType.createdAt) ? cursorEntity.getCreatedAt().toString() : cursorEntity.getLikeCount().toString();
        UUID nextIdAfter = cursorEntity.getId();
        List<FeedDto> data = feedMapper.toFeedDtoList(feedEntityList, user);
        return new FeedDtoCursorResponse(data, nextCursor, nextIdAfter, hasNext, totalCount, nextSortBy, nextDirection);
    }

    // 정렬방식 지정 메서드
    private OrderSpecifier<?>[] getOrderSpecifiers(SortType sortType, SortDirection direction) {

        Order order = direction == SortDirection.DESCENDING ? Order.DESC : Order.ASC;
        // 정렬할 기준필드 설정
        return switch (sortType) {
            // 지정한 정렬 기준필드(작성일 or 좋아요수)가 동일할 경우, id로 정렬
            case createdAt ->
                    new OrderSpecifier<?>[]{new OrderSpecifier<>(order, feed.createdAt), new OrderSpecifier<>(order, feed.id)};
            case likeCount ->
                    new OrderSpecifier<?>[]{new OrderSpecifier<>(order, feed.likeCount), new OrderSpecifier<>(order, feed.id)};
            default ->
                    new OrderSpecifier<?>[]{new OrderSpecifier<>(order, feed.createdAt), new OrderSpecifier<>(order, feed.id)};

        };
    }
}

