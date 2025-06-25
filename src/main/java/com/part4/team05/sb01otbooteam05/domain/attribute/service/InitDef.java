package com.part4.team05.sb01otbooteam05.domain.attribute.service;

import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeDefinition;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.ColorType;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.SizeType;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.StyleType;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.ThicknessType;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.TouchType;
import com.part4.team05.sb01otbooteam05.domain.attribute.repository.AttributeDefinitionRepository;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitDef {
  private final AttributeDefinitionRepository repository;

  @PostConstruct
  public void init() {
    if (repository.count() > 0) return;

    List<AttributeDefinition> defList = List.of(
        new AttributeDefinition("color", enumToStrings(ColorType.values())),
        new AttributeDefinition("size", enumToStrings(SizeType.values())),
        new AttributeDefinition("style", enumToStrings(StyleType.values())),
        new AttributeDefinition("thickness", enumToStrings(ThicknessType.values())),
        new AttributeDefinition("touch", enumToStrings(TouchType.values()))
    );

    repository.saveAll(defList);
  }

  private <E extends Enum<E>> List<String> enumToStrings(E[] values) {
    return Stream.of(values).map(Enum::name).toList();
  }

}
