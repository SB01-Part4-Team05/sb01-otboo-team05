package com.part4.team05.sb01otbooteam05.domain.user.dto;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDtoCursorResponse {

  private List<UserDto> data;

  private String nextCursor;

  private UUID nextIdAfter;

  private boolean hasNext;

  private Long totalCount;

  private String sortBy;

  private String sortDirection;
}
