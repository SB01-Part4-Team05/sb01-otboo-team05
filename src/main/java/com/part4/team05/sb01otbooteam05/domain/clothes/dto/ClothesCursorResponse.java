package com.part4.team05.sb01otbooteam05.domain.clothes.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClothesCursorResponse {
  List<ClothesDto> data; // clothesDtos -> data로 변경
  String nextCursor;
  String nextIdAfter;
  boolean hasNext;
  Integer totalCount;
  String sortBy;
  String sortDirection;
}

