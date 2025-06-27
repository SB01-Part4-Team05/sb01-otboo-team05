package com.part4.team05.sb01otbooteam05.domain.feed.dto.request;

import java.util.UUID;

import org.hibernate.query.SortDirection;

import com.part4.team05.sb01otbooteam05.domain.feed.enums.PrecipitationType;
import com.part4.team05.sb01otbooteam05.domain.feed.enums.SkyStatusType;
import com.part4.team05.sb01otbooteam05.domain.feed.enums.SortType;

public record FindFeedsRequest(
	String cursor,
	UUID idAfter,
	Integer limit,
	SortType sortBy,
	SortDirection sortDirection,
	String keywordLike,
	SkyStatusType skyStatusEqual,
	PrecipitationType precipitationTypeEqual,
	UUID authorIdEqual
) {
}
