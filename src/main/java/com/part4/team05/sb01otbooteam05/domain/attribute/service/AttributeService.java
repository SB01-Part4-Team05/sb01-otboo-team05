package com.part4.team05.sb01otbooteam05.domain.attribute.service;

import com.part4.team05.sb01otbooteam05.domain.attribute.dto.ClothesAttributeDefDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeValue;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeDefinition;
import com.part4.team05.sb01otbooteam05.domain.attribute.exception.NoSuchDefException;
import com.part4.team05.sb01otbooteam05.domain.attribute.mapper.AttributeDefinitionMapper;
import com.part4.team05.sb01otbooteam05.domain.attribute.repository.AttributeDefinitionRepository;
import com.part4.team05.sb01otbooteam05.domain.attribute.repository.AttributeRepository;
import com.part4.team05.sb01otbooteam05.domain.attribute.dto.ClothesAttributeDefCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.attribute.dto.ClothesAttributeDefUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesAttributeDto;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import java.awt.print.Pageable;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.SortDirection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttributeService {
  private final AttributeRepository attributeRepository;
  private final AttributeDefinitionRepository definitionRepository;
  private final AttributeDefinitionMapper definitionMapper;

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
    }).collect(Collectors.toList());
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

  public ClothesAttributeDefDtoCursorResponse getDef(
      UUID cursor,
      int limit,
      UUID idAfter,
      String sortedBy,
      String sortDirection,
      String keywordLike
  ) {
    String sortField = (sortedBy != null && !sortedBy.isBlank()) ? sortedBy : "id";
    Sort.Direction direction;
    try {
      direction = Sort.Direction.fromString(sortDirection);
    } catch (IllegalArgumentException e) {
      direction = Sort.Direction.DESC;
    }

    PageRequest pageable = PageRequest.of(0, limit, Sort.by(direction, sortField));

    List<AttributeDefinition> defs = definitionRepository.findByConditions(cursor, idAfter, keywordLike, pageable);

    ClothesAttributeDefDtoCursorResponse response = new ClothesAttributeDefDtoCursorResponse();
    response.setClothesAttributeDefDtos(definitionMapper.toDtoList(defs));

    UUID nextCursor = defs.isEmpty() ? null : defs.get(defs.size() - 1).getId();
    response.setNextCursor(nextCursor != null ? nextCursor.toString() : null);
    response.setNextIdAfter(nextCursor != null ? nextCursor.toString() : null);
    response.setNextCount(defs.size());
    response.setHasNext(defs.size() == limit);
    response.setSortBy(sortField);
    response.setSortDirection(direction.name());
    response.setTotalCount(definitionRepository.count());

    return response;
  }

  public AttributeDefinition findByDefName(String name){
    return definitionRepository.findByName(name).orElseThrow(()
        -> new NoSuchDefException("해당하는 선택 항목이 없습니다."));
  }

}
