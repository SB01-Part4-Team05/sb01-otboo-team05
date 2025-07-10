package com.part4.team05.sb01otbooteam05.domain.follow.mapper;

import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowDto;
import com.part4.team05.sb01otbooteam05.domain.follow.entity.Follow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FollowMapper {

    public Follow toEntity(FollowCreateRequest request) {
        return new Follow(
                request.followerId(),
                request.followeeId()
        );
    }

    public FollowDto toDto(Follow entity) {
        return new FollowDto(
                entity.getId(),
                entity.getFollowee(),
                entity.getFollower()
        );
    }
}
