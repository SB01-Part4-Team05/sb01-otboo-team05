package com.part4.team05.sb01otbooteam05.domain.feedComment.repository;


import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDto;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.FindCommentsRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.entity.Comment;
import com.part4.team05.sb01otbooteam05.domain.feedComment.entity.QComment;
import com.part4.team05.sb01otbooteam05.domain.feedComment.mapper.CommentMapper;
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

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SearchCommentRepositoryImpl implements SearchCommentRepository {
    private final JPAQueryFactory queryFactory;
    private final CommentMapper commentMapper;
    private final UserRepository userRepository;
    private final QUser author = QUser.user;
    private final QWeather weather = QWeather.weather;
    private final QComment comment = QComment.comment;

    @Override
    public CommentDtoCursorResponse findCommentDtosWithCursor(UUID userId, FindCommentsRequest request) {

        BooleanBuilder builder = new BooleanBuilder();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());


        // 정렬 방식 설정
        // 현재 댓글의 경우 정렬방향과 정렬필드를 선택할 수 있게 되어있지 않아,
        // 프로토타입 설정값에 따라 ASCENDING 방향, createdAt 필드로 지정함.
        OrderSpecifier<?> orderSpecifier = getOrderSpecifier();

        List<Comment> commentEntityList = queryFactory
                .selectFrom(comment)
                .leftJoin(comment.author, author).fetchJoin()
                .where(builder)
                .orderBy(orderSpecifier)
                .limit(request.limit() + 1)
                .fetch();

        Boolean hasNext = (commentEntityList.size() > request.limit());
        String nextCursor = null;
        String nextIdAfter = null;
        SortDirection nextDirection = SortDirection.ASCENDING;
        String nextSortBy = "createdAt";

        // 해당 검색 조건으로 조회한 총 데이터 개수 반환
        Long totalCount = queryFactory
                .select(comment.count())
                .from(comment)
                .where(builder)
                .fetchOne();

        // 다음 페이지가 있다면 커서와 IdAfter 반환하고, 추가로 불러온 데이터 제거
        if (hasNext) {
            Comment lastComment = commentEntityList.get(request.limit());
            nextCursor = lastComment.getCreatedAt().toString();
            nextIdAfter = lastComment.getId().toString();
            commentEntityList.remove(request.limit());
        }

        // hasNext 판별을 위한 추가 데이터가 있다면 제거하고, Dto 변환
        List<CommentDto> data = commentMapper.toCommentDtoList(commentEntityList);
        return new CommentDtoCursorResponse(data, nextCursor, nextIdAfter, hasNext, totalCount, nextSortBy, nextDirection);
    }

    // 정렬방식 지정 메서드
    private OrderSpecifier<?> getOrderSpecifier() {
        Order order = Order.ASC;
        return new OrderSpecifier<>(order, comment.createdAt); // 기본값
    }

}


