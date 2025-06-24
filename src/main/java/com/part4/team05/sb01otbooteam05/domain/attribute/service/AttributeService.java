package com.part4.team05.sb01otbooteam05.domain.attribute.service;

import com.part4.team05.sb01otbooteam05.domain.attribute.dto.AttributeDto;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.Attribute;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeDefinition;
import com.part4.team05.sb01otbooteam05.domain.attribute.exception.NoSuchDefException;
import com.part4.team05.sb01otbooteam05.domain.attribute.mapper.AttributeMapper;
import com.part4.team05.sb01otbooteam05.domain.attribute.repository.AttributeDefinitionRepository;
import com.part4.team05.sb01otbooteam05.domain.attribute.repository.AttributeRepository;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttributeService {
  private final AttributeRepository attributeRepository;
  private final AttributeDefinitionRepository definitionRepository;
  private final AttributeMapper attributeMapper;

  public List<Attribute> createAndReturnList(List<Attribute> attributes, Clothes clothes){
    return attributes.stream().map(attribute ->{
      AttributeDefinition def = definitionRepository.findById(attribute.getDefinition().getId())
          .orElseThrow(()-> new NoSuchDefException("해당하는 선택 가능 항목이 없습니다."));

      return Attribute.builder()
          .definition(def)
          .value(attribute.getValue())
          .clothes(clothes)
          .build();
    }).toList();
  }

}
