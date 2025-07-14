package com.part4.team05.sb01otbooteam05.domain.follow.repository;

import com.part4.team05.sb01otbooteam05.domain.follow.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

    long countByFollowee(UUID followee);

    long countByFollower(UUID follower);

    boolean existsByFollowerAndFollowee(UUID follower, UUID followee);

    @Query("""
    SELECT f
    FROM Follow f
    WHERE f.follower = :userId
    AND (:cursorId IS NULL OR f.id > :cursorId)
    ORDER BY f.createdAt ASC
    """)
    List<Follow> findFollowings(
            @Param("userId") UUID userId,
            @Param("cursorId") UUID cursorId
    );


    List<Follow> findFollowers(UUID followeeId, UUID idAfter, int limit, String nameLike);
}