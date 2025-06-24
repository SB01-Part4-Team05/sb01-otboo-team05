package com.part4.team05.sb01otbooteam05.domain.clothes.service;

import com.part4.team05.sb01otbooteam05.domain.attribute.entity.Attribute;
import com.part4.team05.sb01otbooteam05.domain.attribute.service.AttributeService;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesAttributeDefUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesAttributeUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesDto;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.ClothesType;
import com.part4.team05.sb01otbooteam05.domain.clothes.mapper.ClothesMapper;
import com.part4.team05.sb01otbooteam05.domain.clothes.repository.ClothesRepository;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClothesService {
  private final ClothesMapper mapper;
  private final ClothesRepository repository;
  private final AttributeService attributeService;

  public List<ClothesDto> get(UUID ownerId){
    return repository.findByOwnerId(ownerId);
  }

  @Transactional
  public ClothesDto create(ClothesCreateRequest request){
    Clothes clothes = Clothes.builder()
        .ownerId(request.ownerId())
        .type(ClothesType.valueOf(request.type()))
        .name(request.name())
        .build();

    List<Attribute> list = attributeService.createAndReturnList(request.attributes(),clothes);
    clothes.setAttributes(list);

    repository.save(clothes);

    return mapper.toDto(clothes);
  }

  @Transactional
  public void delete(UUID id){
    Clothes clothes = repository.findById(id).orElseThrow(NoSuchElementException::new);
    attributeService.delete(clothes.getAttributes());

    repository.delete(clothes);
  }

  @Transactional
  public ClothesDto updateAttributes(ClothesAttributeUpdateRequest request){
    Clothes clothes = repository.findById(request.id()).orElseThrow(NoSuchElementException::new);
    List<Attribute> list = clothes.getAttributes();

    for(Attribute a : list){
      if(a.getDefinition().getId().equals(request.definitionId())){
        attributeService.updateValue(a.getId(),request.value());
        break;
      }
    }

    return mapper.toDto(clothes);
  }
}
