package com.part4.team05.sb01otbooteam05.domain.attribute.dto;

import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeValue;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttributeDto {
  private UUID definitionId;
  private String definitionName;
  private List<String> selectableValues;
  private String value;

  public AttributeDto() {
  }

  public AttributeDto(AttributeValue attributeValue) {
    if(attributeValue == null) throw new NullPointerException();

    if(attributeValue.getDefinition() == null) throw new NullPointerException();

    this.definitionId = attributeValue.getDefinition().getId();
    this.definitionName = attributeValue.getDefinition().getName();
    this.selectableValues = attributeValue.getDefinition().getSelectableValues();
    this.value = attributeValue.getValue();
  }
}
