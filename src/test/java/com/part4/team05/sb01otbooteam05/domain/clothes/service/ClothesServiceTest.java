package com.part4.team05.sb01otbooteam05.domain.clothes.service;

import com.part4.team05.sb01otbooteam05.domain.attribute.service.AttributeService;
import com.part4.team05.sb01otbooteam05.domain.auth.security.jwt.JwtTokenProvider;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesDto;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.ClothesType;
import com.part4.team05.sb01otbooteam05.domain.clothes.exception.ClothesNotFoundException;
import com.part4.team05.sb01otbooteam05.domain.clothes.mapper.ClothesMapper;
import com.part4.team05.sb01otbooteam05.domain.clothes.repository.ClothesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ClothesServiceTest {

    @InjectMocks
    ClothesService clothesService;

    @Mock
    ClothesRepository clothesRepository;

    @Mock
    ClothesMapper clothesMapper;

    @Mock
    AttributeService attributeService;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @Test
    void get() {
        UUID ownerId = UUID.randomUUID();
        UUID cursor = UUID.randomUUID();
        given(clothesRepository.findByOwnerIdPageNation(ownerId, cursor,
                Pageable.ofSize(10))).willReturn(List.of(mock(Clothes.class)));
        given(clothesMapper.toDtoList(any(List.class))).willReturn(List.of(mock(ClothesDto.class)));

        ClothesCursorResponse response = clothesService.get(ownerId, cursor, 10);

        assertEquals(1, response.getClothesDtos().size());
    }

    @Test
    void getClothesEntityByIdOrThrow() {
        UUID clothesId = UUID.randomUUID();
        Clothes clothe = Clothes.builder().id(clothesId)
                .name("Clothes1").build();

        given(clothesRepository.findById(clothesId)).willReturn(Optional.ofNullable(clothe));

        Clothes clothes = clothesService.getClothesEntityByIdOrThrow(clothesId);

        assertEquals("Clothes1", clothes.getName());
    }

    @Test
    void getClothesEntityByIdOrThrow_Fail() {
        UUID clothesId = UUID.randomUUID();

        assertThrows(ClothesNotFoundException.class, () -> clothesService.getClothesEntityByIdOrThrow(clothesId));
    }

    @Test
    void create() {
        UUID ownerId = UUID.randomUUID();
        UUID clothesId = UUID.randomUUID();
        ClothesCreateRequest request = new ClothesCreateRequest(ownerId, "clothes1",
                "TOP", Collections.emptyList());
        Clothes clothes = Clothes.builder()
                .id(clothesId)
                .ownerId(ownerId)
                .name(request.name())
                .type(ClothesType.valueOf(request.type())).build();

        ClothesDto dto = new ClothesDto();
        dto.setId(clothesId);
        dto.setName(request.name());
        dto.setOwnerId(ownerId);

        given(clothesRepository.save(any(Clothes.class))).willReturn(clothes);
        given(clothesMapper.toDto(any(Clothes.class))).willReturn(dto);
        given(attributeService.createAndReturnList(any(List.class), any(Clothes.class)))
                .willReturn(Collections.emptyList());

        ClothesDto clothesDto = clothesService.create(request);

        assertEquals(clothesId, clothesDto.getId());
        assertEquals(request.name(), clothesDto.getName());
    }

    @Test
    void delete() {
        UUID clothesId = UUID.randomUUID();

        doNothing().when(clothesRepository).delete(any(Clothes.class));
        given(clothesRepository.findById(clothesId)).willReturn(
                Optional.ofNullable(mock(Clothes.class)));
        doNothing().when(attributeService).delete(any(List.class));

        clothesService.delete(clothesId);

        verify(clothesRepository, times(1)).delete(any(Clothes.class));
    }

    @Test
    void update() {
        UUID clothesId = UUID.randomUUID();

        ClothesUpdateRequest request = new ClothesUpdateRequest(
                "clothes2", "BOTTOM", Collections.emptyList());

        Clothes clothes = Clothes.builder()
                .id(clothesId)
                .name("clothes1")
                .type(ClothesType.valueOf("TOP")).build();

        ClothesDto dto = new ClothesDto();
        dto.setId(clothesId);
        dto.setType(request.type());
        dto.setName(request.name());

        given(clothesRepository.findById(clothesId)).willReturn(Optional.ofNullable(clothes));
        given(clothesMapper.toDto(clothes)).willReturn(dto);

        ClothesDto clothesDto = clothesService.update(clothesId, request, null);

        assertEquals("clothes2", clothesDto.getName());
        assertEquals("BOTTOM", clothesDto.getType());
    }

    @Test
    void findAllByOwnerId() {
        UUID ownerID = UUID.randomUUID();

        given(clothesRepository.findByOwnerId(ownerID))
                .willReturn(List.of(mock(Clothes.class)));

        List<Clothes> clothes = clothesService.findAllByOwnerId(ownerID);

        assertEquals(1, clothes.size());
    }
}
