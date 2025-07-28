package com.part4.team05.sb01otbooteam05.domain.directMessage.service;

import com.part4.team05.sb01otbooteam05.domain.directMessage.dto.DirectMessageCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.directMessage.dto.DirectMessageDto;
import com.part4.team05.sb01otbooteam05.domain.directMessage.dto.DirectMessageDtoCursorResponse;

import java.util.UUID;

public interface DirectMessageService {

    DirectMessageDto sendMessage(DirectMessageCreateRequest request);

    DirectMessageDtoCursorResponse getMessages(UUID currentUserId, UUID opponentId, UUID idAfter, int limit);
}
