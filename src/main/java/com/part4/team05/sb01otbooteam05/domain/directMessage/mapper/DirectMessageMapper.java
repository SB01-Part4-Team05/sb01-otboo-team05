package com.part4.team05.sb01otbooteam05.domain.directMessage.mapper;

import com.part4.team05.sb01otbooteam05.domain.directMessage.dto.DirectMessageDto;
import com.part4.team05.sb01otbooteam05.domain.directMessage.entity.DirectMessage;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserSummary;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class DirectMessageMapper {

    public DirectMessageDto toDto(DirectMessage m) {
        return new DirectMessageDto(
                m.getId(),
                m.getCreatedAt(),
                toUserSummary(m.getSender()),
                toUserSummary(m.getReceiver()),
                m.getContent()
        );
    }

    public UserSummary toUserSummary(User user) {
        return new UserSummary(
                user.getId(),
                user.getName(),
                user.getProfileImageUrl()
        );
    }
}
