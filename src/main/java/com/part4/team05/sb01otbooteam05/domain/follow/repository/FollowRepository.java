package com.part4.team05.sb01otbooteam05.domain.follow.repository;

import com.part4.team05.sb01otbooteam05.domain.follow.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

    long countByFollowee(UUID followee);

    long countByFollower(UUID follower);

    boolean existsByFollowerAndFollowee(UUID follower, UUID followee);

    boolean existsByFolloweeAndFollower(UUID followee, UUID follower);
}