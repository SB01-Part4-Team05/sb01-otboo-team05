package com.part4.team05.sb01otbooteam05.domain.attribute.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.part4.team05.sb01otbooteam05.domain.attribute.dto.ClothesAttributeDefCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.attribute.dto.ClothesAttributeDefDto;
import com.part4.team05.sb01otbooteam05.domain.attribute.dto.ClothesAttributeDefDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.attribute.dto.ClothesAttributeDefUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeDefinition;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeValue;
import com.part4.team05.sb01otbooteam05.domain.attribute.mapper.AttributeDefinitionMapper;
import com.part4.team05.sb01otbooteam05.domain.attribute.repository.AttributeDefinitionRepository;
import com.part4.team05.sb01otbooteam05.domain.attribute.repository.AttributeRepository;
import com.part4.team05.sb01otbooteam05.domain.auth.security.jwt.JwtTokenProvider;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesAttributeDto;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.part4.team05.sb01otbooteam05.domain.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class AttributeServiceTest {
  @InjectMocks
  AttributeService attributeService;

  @Mock
  AttributeDefinitionMapper definitionMapper;

  @Mock
  AttributeRepository attributeRepository;

  @Mock
  AttributeDefinitionRepository attributeDefinitionRepository;

  @Mock
  JwtTokenProvider jwtTokenProvider;

  @Mock
  NotificationService notificationService;

  @Test
  void createAndReturnList() {
    AttributeDefinition attributeDefinition = mock(AttributeDefinition.class);
    Clothes clothes = mock(Clothes.class);
    AttributeValue attributeValue = mock(AttributeValue.class);
    UUID randomUid = UUID.randomUUID();
    ClothesAttributeDto clothesAttributeDto  = new ClothesAttributeDto(randomUid,"value1");

    given(attributeDefinitionRepository.findById(randomUid))
        .willReturn(Optional.ofNullable(attributeDefinition));
    given(attributeRepository.save(any(AttributeValue.class)))
        .willReturn(attributeValue);

    List<AttributeValue> attributeValues = attributeService.createAndReturnList(List.of(clothesAttributeDto)
        ,clothes);

    assertEquals(1, attributeValues.size());
  }

  @Test
  void updateValue() {
    Long id = 1L;

    AttributeValue attributeValue = AttributeValue.builder()
        .id(id)
        .value("test1")
        .clothes(mock(Clothes.class))
        .definition(mock(AttributeDefinition.class))
        .build();

    given(attributeRepository.findById(id)).willReturn(Optional.ofNullable(attributeValue));

    attributeService.updateValue(1L,"test2");

    assertEquals("test2", Objects.requireNonNull(attributeValue).getValue());
  }

  @Test
  void delete() {
    doNothing().when(attributeRepository).deleteAll(any(List.class));

    attributeService.delete(List.of(mock(AttributeValue.class)));

    verify(attributeRepository, times(1)).deleteAll(any(List.class));
  }

  @Test
  void createDef() {
    ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest("name1",
        Collections.emptyList());

    AttributeDefinition savedDefinition = AttributeDefinition.builder()
        .id(UUID.randomUUID())
        .name("name1")
        .selectableValues(Collections.emptyList())
        .build();

    UUID userId = UUID.randomUUID();

    doNothing().when(notificationService).createAndSendNotification(
            any(), any(), any(), any()
    );

    given(attributeDefinitionRepository.save(any(AttributeDefinition.class)))
        .willReturn(savedDefinition);

    AttributeDefinition definition=
        attributeService.createDef(request,userId);

    assertEquals("name1",definition.getName());
  }

  @Test
  void updateDef() {

    UUID id = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest("name1"
    ,List.of("test1"));

    AttributeDefinition attributeDefinition = AttributeDefinition.builder().id(id)
        .name("test2").build();

    doNothing().when(notificationService).createAndSendNotification(
            any(), any(), any(), any()
    );

    given(attributeDefinitionRepository.findById(id)).willReturn(
        Optional.ofNullable(attributeDefinition));

    AttributeDefinition definition = attributeService.updateDef(id,request,userId);

    assertEquals("name1",definition.getName());
    assertEquals(List.of("test1"),definition.getSelectableValues());
  }

  @Test
  void deleteDef() {
    UUID id = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    AttributeDefinition definition = AttributeDefinition.builder()
            .id(id)
            .name("test")
            .selectableValues(List.of("a", "b"))
            .build();

    given(attributeDefinitionRepository.findById(id)).willReturn(Optional.of(definition));

    doNothing().when(attributeDefinitionRepository).deleteById(id);

    attributeService.deleteDef(id,userId);

    verify(attributeDefinitionRepository, times(1))
        .deleteById(any(UUID.class));
  }

  @Test
  void getDef() {
    UUID cursor = UUID.randomUUID();

    given(definitionMapper.toDtoList(any(List.class))).willReturn(List.of(mock(
        ClothesAttributeDefDto.class)));

    ClothesAttributeDefDtoCursorResponse response = attributeService.getDef(cursor,10,null,null,null,null);

    assertEquals(1,response.getClothesAttributeDefDtos().size());
  }

  @Test
  void findByDefName() {
    AttributeDefinition attributeDefinition = AttributeDefinition.builder()
        .id(UUID.randomUUID())
        .name("test1").build();

    given(attributeDefinitionRepository.findByName(any(String.class))).willReturn(
        Optional.ofNullable(attributeDefinition));

    AttributeDefinition definition = attributeService.findByDefName("test1");

    assertEquals(attributeDefinition.getId(),definition.getId());
  }
}