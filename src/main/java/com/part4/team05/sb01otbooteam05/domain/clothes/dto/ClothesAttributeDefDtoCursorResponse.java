package com.part4.team05.sb01otbooteam05.domain.clothes.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClothesAttributeDefDtoCursorResponse {
  List<ClothesAttributeDefDto> clothesAttributeDefDtos;
  String nextCursor;
  String nextIdAfter;
  boolean hasNext;
  Integer nextCount;
  String sortBy;
  String sortDirection;


}
