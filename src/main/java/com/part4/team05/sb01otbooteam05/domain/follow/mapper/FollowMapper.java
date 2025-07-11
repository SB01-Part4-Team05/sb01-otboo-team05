package com.part4.team05.sb01otbooteam05.domain.follow.mapper;

import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowDto;
import com.part4.team05.sb01otbooteam05.domain.follow.entity.Follow;
import com.part4.team05.sb01otbooteam05.exception.ErrorCode;
import com.part4.team05.sb01otbooteam05.exception.OtbooException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FollowMapper {

    public Follow toEntity(FollowCreateRequest request) {
        if(request == null) {
            throw new OtbooException(ErrorCode.INVALID_REQUEST);
        }

        if(request.followerId() == null || request.followeeId() == null) {
            throw new OtbooException(ErrorCode.INVALID_REQUEST);
        }

        return new Follow(
                request.followerId(),
                request.followeeId()
        );
    }

    public FollowDto toDto(Follow entity) {
        if(entity == null) {
            throw new OtbooException(ErrorCode.INVALID_REQUEST);
        }

        return new FollowDto(
                entity.getId(),
                entity.getFollowee(),
                entity.getFollower()
        );
    }
}
