package com.part4.team05.sb01otbooteam05.domain.recommend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.auth.security.jwt.JwtTokenProvider;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesDto;
import com.part4.team05.sb01otbooteam05.domain.recommend.dto.RecommendationDto;
import com.part4.team05.sb01otbooteam05.domain.recommend.service.RecommendService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(controllers = RecommendController.class)
@TestPropertySource(properties = {
    "ACTUATOR_PASSWORD=dummyPasswordForTest",
    "ACTUATOR_USER=dummyUserForTest"
})
class RecommendControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockitoBean
  RecommendService recommendService;

  @MockitoBean
  JpaMetamodelMappingContext context;

  @MockitoBean
  JwtTokenProvider jwtTokenProvider;

  @Test
  void getRecommendSet() throws Exception {
    UUID weatherId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    CustomUserDetails userDetails = mock(CustomUserDetails.class);
    given(userDetails.getUserId()).willReturn(userId);
    Authentication authentication = new UsernamePasswordAuthenticationToken(
        userDetails, null, List.of(() -> "ROLE_USER"));

    ClothesDto clothesDto = new ClothesDto();
    clothesDto.setType("TOP");
    List<ClothesDto> mockClothes = List.of(clothesDto);

    RecommendationDto dto = new RecommendationDto(weatherId, userId, mockClothes);

    given(recommendService.getRecommend(any(), any())).willReturn(dto);

    MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/recommendations")
            .with(authentication(authentication))
            .with(csrf())
            .param("weatherId", weatherId.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn();

    String responseJson = result.getResponse().getContentAsString();

    JsonNode root = objectMapper.readTree(responseJson);
    JsonNode clothesNode = root.get("clothes");

    assertNotNull(clothesNode, "'clothes' 필드가 응답에 없습니다.");
    assertTrue(clothesNode.isArray());
    assertEquals(1, clothesNode.size());

    List<ClothesDto> parsedClothes = objectMapper.readValue(
        clothesNode.toString(),
        new TypeReference<List<ClothesDto>>() {}
    );

    assertEquals(1, parsedClothes.size());
  }
}
