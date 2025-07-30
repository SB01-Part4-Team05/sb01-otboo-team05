package com.part4.team05.sb01otbooteam05.domain.clothes.service;


import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeValue;
import com.part4.team05.sb01otbooteam05.domain.attribute.service.AttributeService;
import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesDto;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.ClothesType;
import com.part4.team05.sb01otbooteam05.domain.clothes.exception.ClothesNotFoundException;
import com.part4.team05.sb01otbooteam05.domain.clothes.mapper.ClothesMapper;
import com.part4.team05.sb01otbooteam05.domain.clothes.repository.ClothesRepository;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClothesService {
  private final ClothesMapper clothesMapper;
  private final ClothesRepository clothesRepository;
  private final AttributeService attributeService;
  private final ClothesS3Service clothesS3Service;


  public ClothesCursorResponse get(UUID ownerId, UUID cursor, int limit, String typeEqual) {
    Pageable pageable = PageRequest.of(0, limit);

    ClothesType type = null;
    if (typeEqual != null && !typeEqual.isEmpty()) {
      type = ClothesType.valueOf(typeEqual);
    }

    List<Clothes> clothes = clothesRepository.findByOwnerIdPageNation(ownerId, cursor, type, pageable);

    log.info("조회된 옷 개수: {}, ownerId: {}, type: {}", clothes.size(), ownerId, type);

    List<ClothesDto> result = clothesMapper.toDtoList(clothes);
    UUID nextCursor = clothes.isEmpty() ? null : clothes.get(clothes.size() - 1).getId();

    ClothesCursorResponse response = new ClothesCursorResponse();
    response.setData(result);
    response.setNextCursor(nextCursor != null ? nextCursor.toString() : null);
    response.setNextIdAfter(nextCursor != null ? nextCursor.toString() : null);
    response.setTotalCount(result.size());
    response.setHasNext(result.size() == limit);
    response.setSortBy("id");
    response.setSortDirection("DESCENDING");

    return response;
  }


  @Transactional(readOnly = true)
  public Clothes getClothesEntityByIdOrThrow(UUID clothesId) {
    return clothesRepository.findById(clothesId).orElseThrow(()-> new ClothesNotFoundException());
  }
  @Transactional(readOnly = true)
  public Optional<Clothes> getClothesEntityById(UUID clothesId) {
    return clothesRepository.findById(clothesId);
  }

  @Transactional
  public ClothesDto create(ClothesCreateRequest request, MultipartFile image){
    Clothes clothes = Clothes.builder()
        .ownerId(request.ownerId())
        .type(ClothesType.valueOf(request.type()))
        .name(request.name())
        .build();

    List<AttributeValue> list = attributeService.createAndReturnList(request.attributes(), clothes);
    clothes.setAttributeValues(list);

    clothesRepository.save(clothes);

    if(image != null && !image.isEmpty()){
      String url = clothesS3Service.upload(clothes.getId(), image);
      clothes.setImageUrl(url);
      clothesRepository.save(clothes);
    }

    return clothesMapper.toDto(clothes);
  }

  @Transactional
  public void delete(UUID id){
    Clothes clothes = clothesRepository.findById(id).orElseThrow(NoSuchElementException::new);
    if(clothes.getImageUrl() != null) clothesS3Service.delete(clothes.getImageUrl());
    attributeService.delete(clothes.getAttributeValues());

    clothesRepository.delete(clothes);
  }

  @Transactional
  public ClothesDto update(UUID clothesId, ClothesUpdateRequest request, MultipartFile image) {
    Clothes clothes = clothesRepository.findById(clothesId)
        .orElseThrow(NoSuchElementException::new);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
      UUID userId = userDetails.getUserId();
      if (!clothes.getOwnerId().equals(userId)) {
        throw new RuntimeException("옷 소유자만 수정할 수 있습니다.");
      }
    }


    if (request.selectableValues() != null) {
      for (AttributeValue attributeValue : clothes.getAttributeValues()) {
        request.selectableValues().stream()
            .filter(dto -> dto.getDefinitionId().equals(attributeValue.getDefinition().getId()))
            .findFirst()
            .ifPresent(dto -> {
              attributeValue.setValue(dto.getValue());
            });
      }
    }

    if (request.name() != null) {
      clothes.setName(request.name());
    }
    if (request.type() != null) {
      clothes.setType(ClothesType.valueOf(request.type()));
    }

    if (image != null && !image.isEmpty()) {
      String url = clothesS3Service.upload(clothesId, image);
      clothes.setImageUrl(url);
    }

    return clothesMapper.toDto(clothes);
  }

  public List<Clothes> findAllByOwnerId(UUID ownerId){
    return clothesRepository.findByOwnerIdWithAttributes(ownerId);
  }

}

