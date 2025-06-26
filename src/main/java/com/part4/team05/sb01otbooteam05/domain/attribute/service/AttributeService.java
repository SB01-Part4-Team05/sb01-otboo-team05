package com.part4.team05.sb01otbooteam05.domain.attribute.service;

import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeValue;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeDefinition;
import com.part4.team05.sb01otbooteam05.domain.attribute.exception.NoSuchDefException;
import com.part4.team05.sb01otbooteam05.domain.attribute.repository.AttributeDefinitionRepository;
import com.part4.team05.sb01otbooteam05.domain.attribute.repository.AttributeRepository;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesAttributeDefCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesAttributeDefUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesAttributeDto;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttributeService {
  private final AttributeRepository attributeRepository;
  private final AttributeDefinitionRepository definitionRepository;

  @Transactional
  public List<AttributeValue> createAndReturnList(List<ClothesAttributeDto> attributes, Clothes clothes){
    return attributes.stream().map(attribute ->{
      AttributeDefinition def = definitionRepository.findById(attribute.definitionId())
          .orElseThrow(()-> new NoSuchDefException("해당하는 선택 가능 항목이 없습니다."));

      AttributeValue att = AttributeValue.builder()
          .definition(def)
          .value(attribute.value())
          .clothes(clothes)
          .build();
      attributeRepository.save(att);

      return att;
    }).toList();
  }

  @Transactional
  public void updateValue(Long id, String value){
    AttributeValue attributeValue = attributeRepository.findById(id).orElseThrow(NoSuchElementException::new);

    attributeValue.setValue(value);
  }

  @Transactional
  public void delete(List<AttributeValue> attributeValues){
    attributeRepository.deleteAll(attributeValues);
  }

  @Transactional
  public AttributeDefinition createDef(ClothesAttributeDefCreateRequest request){
      AttributeDefinition attributeDefinition = AttributeDefinition.builder()
          .name(request.name())
          .selectableValues(request.selectableValues())
          .build();

      definitionRepository.save(attributeDefinition);

      return attributeDefinition;
  }

  @Transactional
  public AttributeDefinition updateDef(UUID definitionId,ClothesAttributeDefUpdateRequest request){
    AttributeDefinition attributeDefinition = definitionRepository.findById(definitionId)
        .orElseThrow(() -> new NoSuchDefException("해당하는 속성이 없습니다."));

    attributeDefinition.setName(request.name());
    attributeDefinition.setSelectableValues(request.selectableValues());

    return attributeDefinition;
  }

  @Transactional
  public void deleteDef(UUID definitionId){
    definitionRepository.deleteById(definitionId);
  }

  public List<AttributeDefinition> getDef(int limit){
    return definitionRepository.findAll();
  }

  public AttributeDefinition findByDefName(String name){
    return definitionRepository.findByName(name).orElseThrow(()
        -> new NoSuchDefException("해당하는 선택 항목이 없습니다."));
  }

}
