package com.part4.team05.sb01otbooteam05.domain.attribute.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClothesAttributeDefDtoCursorResponse {
  @JsonProperty("data")
  List<ClothesAttributeDefDto> clothesAttributeDefDtos;
  String nextCursor;
  String nextIdAfter;
  boolean hasNext;
  Integer nextCount;
  String sortBy;
  String sortDirection;


}
