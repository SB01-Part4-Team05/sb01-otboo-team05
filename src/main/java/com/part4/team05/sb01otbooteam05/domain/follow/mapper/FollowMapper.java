package com.part4.team05.sb01otbooteam05.domain.follow.mapper;

import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowDto;
import com.part4.team05.sb01otbooteam05.domain.follow.entity.Follow;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserSummary;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.exception.ErrorCode;
import com.part4.team05.sb01otbooteam05.exception.OtbooException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FollowMapper {

    public Follow toEntity(FollowCreateRequest request) {
        if(request == null || request.followerId() == null || request.followeeId() == null) {
            throw new OtbooException(ErrorCode.INVALID_REQUEST);
        }

        return new Follow(
                request.followerId(),
                request.followeeId()
        );
    }

    public FollowDto toDto(Follow entity, Map<UUID, User> userMap) {
        if(entity == null) {
            throw new OtbooException(ErrorCode.INVALID_REQUEST);
        }

        User follower = userMap.get(entity.getFollower());
        User followee = userMap.get(entity.getFollowee());

        if(follower == null || followee == null) {
            throw new OtbooException(ErrorCode.USER_NOT_FOUND);
        }

        return new FollowDto(
                entity.getId(),
                toUserSummary(followee),
                toUserSummary(follower)
        );
    }

    private UserSummary toUserSummary(User user) {
        return new UserSummary(
                user.getId(),
                user.getName(),
                user.getProfileImageUrl()
        );
    }
}
