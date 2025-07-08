package com.part4.team05.sb01otbooteam05.domain.notification.dto;

import java.util.List;
import java.util.UUID;

public record NotificationDtoCursorResponse (
        List<NotificationDto> data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        long totalCount,
        String sortBy,
        String sortDirection
) {
}
