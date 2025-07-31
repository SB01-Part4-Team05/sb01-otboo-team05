package com.part4.team05.sb01otbooteam05.domain.attribute.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team05.sb01otbooteam05.domain.attribute.dto.ClothesAttributeDefDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeDefinition;
import com.part4.team05.sb01otbooteam05.domain.attribute.service.AttributeService;
import com.part4.team05.sb01otbooteam05.domain.attribute.dto.ClothesAttributeDefCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.attribute.dto.ClothesAttributeDefUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.auth.security.jwt.JwtTokenProvider;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(controllers = AttributeController.class)
class AttributeControllerTest {


  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockitoBean
  AttributeService attributeService;

  @MockitoBean
  JpaMetamodelMappingContext metamodelMappingContext;

  @MockitoBean
  JwtTokenProvider jwtTokenProvider;

  @Test
  @DisplayName("속성 추가 성공 테스트")
  void createDefSuccess() throws Exception{
    ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest("test1",
        List.of("a","b","c"));

    CustomUserDetails customUserDetails = new CustomUserDetails(
            UUID.randomUUID(),
            "admin@otboo.com",
            "ADMIN"
    );

    mockMvc.perform(MockMvcRequestBuilders.post("/api/clothes/attribute-defs")
                    .with(csrf())
                    .with(user(customUserDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("유저 권한 부족 실패 테스트")
  @WithMockUser
  void creatDefNoAuth() throws Exception{
    ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest("test1",
        List.of("a","b","c"));

    mockMvc.perform(MockMvcRequestBuilders.post("/api/clothes/attribute-defs")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("속성 업데이트 성공 테스트")
  void updateSuccess() throws Exception {
    UUID id = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest("test1",
        List.of("d","e","f"));

    AttributeDefinition mockDefinition = mock(AttributeDefinition.class);

    given(attributeService.updateDef(eq(id), eq(request), eq(userId))).willReturn(mockDefinition);

    CustomUserDetails customUserDetails = new CustomUserDetails(userId, "admin@otboo.com", "ADMIN");

    mockMvc.perform(MockMvcRequestBuilders.patch("/api/clothes/attribute-defs/" + id)
                    .with(csrf())
                    .with(user(customUserDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  @DisplayName("속성 업데이트 유저 권한 부족 실패 테스트")
  void updateForbiddenFail() throws Exception {
    AttributeDefinition attributeDefinition = mock(AttributeDefinition.class);

    ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest("test1",
        List.of("d","e","f"));

    UUID id = UUID.randomUUID();
    given(attributeService.updateDef(eq(id), eq(request), any(UUID.class)))
            .willReturn(attributeDefinition);

    mockMvc.perform(MockMvcRequestBuilders.patch("/api/clothes/attribute-defs/"+ id)
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("속성 삭제 성공 테스트")
  void deleteDef() throws Exception {
    UUID id = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    CustomUserDetails customUserDetails = new CustomUserDetails(userId, "admin@otboo.com", "ADMIN");

    doNothing().when(attributeService).deleteDef(eq(id), any(UUID.class));

    mockMvc.perform(MockMvcRequestBuilders
            .delete("/api/clothes/attribute-defs/"+ UUID.randomUUID())
            .with(csrf())
            .with(user(customUserDetails)))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser
  @DisplayName("속성 리스트 가져 오기")
  void getDef() throws Exception {

    ClothesAttributeDefDtoCursorResponse mockResponse = mock(ClothesAttributeDefDtoCursorResponse.class);

    given(attributeService.getDef(null, 10,null,null,null,null))
    .willReturn(mockResponse);


    mockMvc.perform(MockMvcRequestBuilders
        .get("/api/clothes/attribute-defs")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(attributeService.getDef(null,10,null,null,null,null))))
        .andExpect(status().isOk());

  }
}