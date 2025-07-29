package com.part4.team05.sb01otbooteam05.domain.clothes.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team05.sb01otbooteam05.domain.attribute.service.AttributeService;
import com.part4.team05.sb01otbooteam05.domain.auth.security.jwt.JwtTokenProvider;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesDto;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import com.part4.team05.sb01otbooteam05.domain.clothes.mapper.ClothesMapper;
import com.part4.team05.sb01otbooteam05.domain.clothes.service.ClothesService;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(controllers = ClothesController.class)
class ClothesControllerTest {
  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockitoBean
  ClothesService clothesService;

  @MockitoBean
  ClothesMapper clothesMapper;

  @MockitoBean
  AttributeService attributeService;

  @MockitoBean
  JpaMetamodelMappingContext context;

  @MockitoBean
  JwtTokenProvider jwtTokenProvider;

  @Test
  @WithMockUser
  void getClothes() throws Exception {
    UUID ownerId = UUID.randomUUID();

    given(clothesService.get(eq(ownerId), isNull(), eq(10), isNull()))
        .willReturn(mock(ClothesCursorResponse.class));

    mockMvc.perform(MockMvcRequestBuilders
            .get("/api/clothes")
            .param("ownerId", ownerId.toString())
            .param("limit", "10")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void getClothesWithTypeFilter() throws Exception {
    UUID ownerId = UUID.randomUUID();
    String typeEqual = "TOP";

    given(clothesService.get(eq(ownerId), isNull(), eq(10), eq(typeEqual)))
        .willReturn(mock(ClothesCursorResponse.class));

    mockMvc.perform(MockMvcRequestBuilders
            .get("/api/clothes")
            .param("ownerId", ownerId.toString())
            .param("limit", "10")
            .param("typeEqual", typeEqual)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void getClothesWithCursor() throws Exception {
    UUID ownerId = UUID.randomUUID();
    UUID cursor = UUID.randomUUID();

    given(clothesService.get(eq(ownerId), eq(cursor), eq(10), isNull()))
        .willReturn(mock(ClothesCursorResponse.class));

    mockMvc.perform(MockMvcRequestBuilders
            .get("/api/clothes")
            .param("ownerId", ownerId.toString())
            .param("cursor", cursor.toString())
            .param("limit", "10")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void saveClothes() throws Exception {
    UUID ownerId = UUID.randomUUID();
    ClothesCreateRequest request = new ClothesCreateRequest(ownerId,"clothesname1"
        ,"TOP", Collections.emptyList());

    ClothesDto clothesDto = new ClothesDto();
    clothesDto.setName(request.name());

    given(clothesService.create(request, null)).willReturn(clothesDto);
    given(attributeService.createAndReturnList(Collections.emptyList(),new Clothes()))
        .willReturn(Collections.emptyList());

    MockMultipartFile requestPart = new MockMultipartFile(
        "request",
        "",
        "application/json",
        objectMapper.writeValueAsBytes(request)
    );

    MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/clothes")
            .file(requestPart)
            .with(csrf()))
        .andExpect(status().isCreated())
        .andReturn();

    ClothesDto response = objectMapper
        .readValue(result.getResponse().getContentAsString()
            ,ClothesDto.class);

    assertNotNull(response);
    assertEquals("clothesname1",response.getName());
  }

  @Test
  @WithMockUser
  void delete() throws Exception {
    UUID clothesId = UUID.randomUUID();

    doNothing().when(clothesService).delete(clothesId);

    mockMvc.perform(MockMvcRequestBuilders.delete("/api/clothes/"+clothesId)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser
  void patchClothes() throws Exception {
    UUID clothesId = UUID.randomUUID();
    ClothesUpdateRequest request = new ClothesUpdateRequest("clothes1",
        "BOTTOM",
        Collections.emptyList());

    ClothesDto clothesDto = new ClothesDto();
    clothesDto.setId(clothesId);
    clothesDto.setName("clothes1");

    given(clothesService.update(clothesId,request,null)).willReturn(clothesDto);

    MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/clothes/" + clothesId)
            .file(new MockMultipartFile(
                "request",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request)
            ))
            .with(csrf())
            .with(request1 -> {
              request1.setMethod("PATCH");
              return request1;
            }))
        .andExpect(status().isOk())
        .andReturn();

    ClothesDto response = objectMapper.readValue(result.getResponse().getContentAsString()
        ,ClothesDto.class);

    assertEquals("clothes1",response.getName());
    assertEquals(clothesId,response.getId());
  }
}

