package com.part4.team05.sb01otbooteam05.domain.follow.repository;

import com.part4.team05.sb01otbooteam05.domain.follow.entity.Follow;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

    // 팔로워 수 조회
    long countByFollowee(UUID followee);

    // 팔로잉 수 조회
    long countByFollower(UUID follower);

    // 특정 팔로우 관계 존재 여부
    boolean existsByFollowerAndFollowee(UUID follower, UUID followee);

    // 팔로잉 목록 조회
    @Query("""
    SELECT f
    FROM Follow f
    JOIN User u ON f.followee = u.id
    WHERE f.follower = :followerId
      AND (:idAfter IS NULL OR f.id > :idAfter)
      AND (:nameLike IS NULL OR u.name LIKE %:nameLike%)
    ORDER BY f.createdAt ASC
""")
    List<Follow> findFollowings(
            @Param("followerId") UUID followerId,
            @Param("idAfter") UUID idAfter,
            @Param("nameLike") String nameLike,
            Pageable pageable
    );

    // 팔로워 목록 조회
    @Query("""
    SELECT f
    FROM Follow f
    JOIN User u ON f.follower = u.id
    WHERE f.followee = :followeeId
      AND (:idAfter IS NULL OR f.id > :idAfter)
      AND (:nameLike IS NULL OR u.name LIKE %:nameLike%)
    ORDER BY f.createdAt ASC
""")
    List<Follow> findFollowers(
            @Param("followeeId") UUID followeeId,
            @Param("idAfter") UUID idAfter,
            @Param("nameLike") String nameLike,
            Pageable pageable
    );
}