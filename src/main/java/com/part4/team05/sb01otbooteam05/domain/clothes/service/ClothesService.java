package com.part4.team05.sb01otbooteam05.domain.clothes.service;

import com.part4.team05.sb01otbooteam05.domain.attribute.entity.Attribute;
import com.part4.team05.sb01otbooteam05.domain.attribute.service.AttributeService;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesDto;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.ClothesType;
import com.part4.team05.sb01otbooteam05.domain.clothes.mapper.ClothesMapper;
import com.part4.team05.sb01otbooteam05.domain.clothes.repository.ClothesRepository;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClothesService {
  private final ClothesMapper mapper;
  private final ClothesRepository repository;
  private final AttributeService attributeService;

  public List<ClothesDto> get(){
    return Collections.emptyList();
  }

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
}
