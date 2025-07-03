package com.part4.team05.sb01otbooteam05.domain.clothes.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.part4.team05.sb01otbooteam05.domain.attribute.dto.AttributeDto;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeValue;
import com.part4.team05.sb01otbooteam05.domain.attribute.service.AttributeService;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesDto;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.ClothesType;
import com.part4.team05.sb01otbooteam05.domain.clothes.mapper.ClothesMapper;
import com.part4.team05.sb01otbooteam05.domain.clothes.repository.ClothesRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClothesService {
  private final ClothesMapper clothesMapper;
  private final ClothesRepository clothesRepository;
  private final AttributeService attributeService;

  public List<ClothesDto> get(UUID ownerId){ // slice(cursor pagination) 변경 필요
    List<Clothes> clothes = clothesRepository.findByOwnerId(ownerId);
    List<ClothesDto> result = new ArrayList<>();

    for(Clothes c : clothes){
      List<AttributeValue> attributeValues = c.getAttributeValues();

      ClothesDto cd = clothesMapper.toDto(c);
      cd.setAttributes(attributeValues.stream()
          .map(AttributeDto::new)
          .toList());
      result.add(cd);
    }

    return result;
  }

  @Transactional
  public ClothesDto create(ClothesCreateRequest request){
    Clothes clothes = Clothes.builder()
        .ownerId(request.ownerId())
        .type(ClothesType.valueOf(request.type()))
        .name(request.name())
        .build();

    List<AttributeValue> list = attributeService.createAndReturnList(request.attributes(),clothes);
    clothes.setAttributeValues(list);

    clothesRepository.save(clothes);

    return clothesMapper.toDto(clothes);
  }

  @Transactional
  public void delete(UUID id){
    Clothes clothes = clothesRepository.findById(id).orElseThrow(NoSuchElementException::new);
    attributeService.delete(clothes.getAttributeValues());

    clothesRepository.delete(clothes);
  }

  @Transactional
  public ClothesDto update(UUID clothesId, ClothesUpdateRequest request, MultipartFile image){
    Clothes clothes = clothesRepository.findById(clothesId).orElseThrow(NoSuchElementException::new);

    if(request.name() != null) {
      clothes.setName(request.name());
    }
    if(request.type() != null){
      clothes.setType(ClothesType.valueOf(request.type()));
    }

    if(!image.isEmpty()){
      String url = storeImage(image);
      clothes.setImageUrl(url);
    }

    if(request.selectableValues() != null){
      clothes.setAttributeValues(request.selectableValues().stream()
          .map(attributeDto -> AttributeValue.builder()
              .value(attributeDto.getValue())
              .definition(attributeService.findByDefName(attributeDto.getDefinitionName()))
              .clothes(clothes)
              .build()).toList());
    }

    return clothesMapper.toDto(clothes);
  }

  private String storeImage(MultipartFile file){
    try{
      String fileName = UUID.randomUUID() + ".jpg";
      Path path = Paths.get("/home/ubuntu/app/images", fileName);
      Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

      return ServletUriComponentsBuilder.fromCurrentContextPath()
          .path("/images/")
          .path(fileName)
          .toUriString();

    }catch (IOException e){
      e.printStackTrace();
    }

    throw new RuntimeException("이미지 파일 저장에 실패하였습니다.");
  }

  public List<Clothes> findAllByOwnerId(UUID ownerId){
    return clothesRepository.findByOwnerId(ownerId);
  }

}
