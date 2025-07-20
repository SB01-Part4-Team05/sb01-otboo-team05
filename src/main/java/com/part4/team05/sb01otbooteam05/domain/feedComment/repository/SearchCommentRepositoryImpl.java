package com.part4.team05.sb01otbooteam05.domain.feedComment.repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.hibernate.query.SortDirection;
import org.springframework.stereotype.Repository;

import com.part4.team05.sb01otbooteam05.domain.feed.enums.SortType;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDto;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.FindCommentsRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.entity.Comment;
import com.part4.team05.sb01otbooteam05.domain.feedComment.entity.QComment;
import com.part4.team05.sb01otbooteam05.domain.feedComment.mapper.CommentMapper;
import com.part4.team05.sb01otbooteam05.domain.user.entity.QUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SearchCommentRepositoryImpl implements SearchCommentRepository {
    private final JPAQueryFactory queryFactory;
    private final CommentMapper commentMapper;
    private final QUser author = QUser.user;
    private final QComment comment = QComment.comment;

    @Override
    public CommentDtoCursorResponse findCommentDtosWithCursor(UUID userId, FindCommentsRequest request) {

        BooleanBuilder builder = new BooleanBuilder();

        // 현재 유저까지 확인할 필요는 없으나, 추후 댓글 수정/삭제 기능 추가 가능성을 고려해 유저검증로직을 만들어 주석처리해두었음.
        // User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());

        // 피드 조건 추가
        if (request.feedId() != null) {
            builder.and(comment.feed.id.eq(request.feedId()));
        }

        // 커서가 존재할 경우 커서 기반 페이지네이션 조건 추가
        if ((request.cursor()!=null) && (request.idAfter() != null)) {
                // 생성일 기준 오래된 순으로 정렬
                LocalDateTime cursorTime = request.cursor();
                builder.and(
                    comment.createdAt.gt(cursorTime) // 커서 시간 초과의 데이터만 필터링
                        .or(comment.createdAt.eq(cursorTime)
                            .and(comment.id.gt(request.idAfter()))) // 커서시간 동일할 시 ID로 필터링
                );
        }

        // 정렬 방식 설정
        // 현재 댓글의 경우 정렬방향과 정렬필드를 선택할 수 있게 되어있지 않아,
        // 프로토타입 설정값에 따라 ASCENDING 방향, createdAt 필드로 지정함.
        OrderSpecifier<?>[] orderSpecifier = getOrderSpecifiers();

        List<Comment> commentEntityList = queryFactory
                .selectFrom(comment)
                .leftJoin(comment.author, author).fetchJoin()
                .where(builder)
                .orderBy(orderSpecifier)
                .limit(request.limit() + 1)
                .fetch();

        // 해당 검색 조건으로 조회한 총 데이터 개수 반환
        Long totalCount = queryFactory
            .select(comment.count())
            .from(comment)
            .where(builder)
            .fetchOne();

        Boolean hasNext = (commentEntityList.size() > request.limit());
        SortDirection nextSortDirection = SortDirection.ASCENDING;
        SortType nextSortBy = SortType.createdAt;

        // 다음 페이지가 있다면 추가로 불러온 데이터 제거
        if (hasNext) {commentEntityList.remove(request.limit());}
        // 검색된 피드가 없다면 빈 데이터 반환
        if (commentEntityList.isEmpty()){
            return new CommentDtoCursorResponse(Collections.emptyList(), null, null, false, totalCount, nextSortBy, nextSortDirection);
        }

        // 검색값의 마지막 객체가 커서로서 사용된다.
        Comment cursorEntity = commentEntityList.get(commentEntityList.size()-1);
        // 정렬필드에 따라 생성일 or 좋아요 수가 커서가 된다.
        LocalDateTime nextCursor = cursorEntity.getCreatedAt();
        UUID nextIdAfter = cursorEntity.getId();
        List<CommentDto> data = commentMapper.toCommentDtoList(commentEntityList);
        return new CommentDtoCursorResponse(data, nextCursor, nextIdAfter, hasNext, totalCount, nextSortBy, nextSortDirection);
    }

    // 정렬방식 지정 메서드
    private OrderSpecifier<?>[] getOrderSpecifiers() {
        Order order = Order.ASC;
        // 오래된순으로 정렬하며, 동일한 생성일이 존재할 시 id를 기준으로 정렬함.
        return new OrderSpecifier<?>[]{
            new OrderSpecifier<>(order, comment.createdAt),
            new OrderSpecifier<>(order, comment.id)
        };// 기본값
    }

}

