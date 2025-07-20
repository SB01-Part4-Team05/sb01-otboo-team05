package com.part4.team05.sb01otbooteam05.domain.directMessage.service;

import com.part4.team05.sb01otbooteam05.domain.directMessage.dto.DirectMessageCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.directMessage.dto.DirectMessageDto;
import com.part4.team05.sb01otbooteam05.domain.directMessage.dto.DirectMessageDtoCursorResponse;

import java.util.UUID;

public interface DirectMessageService {

    DirectMessageDto sendMessage(DirectMessageCreateRequest request);

    DirectMessageDtoCursorResponse getMessages(UUID userId1, UUID userId2, UUID idAfter, int limit, String sortBy,
                                               String direction);
}
