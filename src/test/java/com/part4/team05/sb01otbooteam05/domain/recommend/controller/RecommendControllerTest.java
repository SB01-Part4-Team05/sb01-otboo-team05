package com.part4.team05.sb01otbooteam05.domain.recommend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team05.sb01otbooteam05.config.SecurityConfig;
import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.auth.security.jwt.JwtTokenProvider;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesDto;
import com.part4.team05.sb01otbooteam05.domain.recommend.service.RecommendService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(controllers = RecommendController.class)
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

    List<List<ClothesDto>> result = List.of(List.of(mock(ClothesDto.class)));
    given(recommendService.getRecommend(any(UUID.class), any(UUID.class)))
        .willReturn(result);

    MvcResult response = mockMvc.perform(MockMvcRequestBuilders.get("/api/recommendations")
            .with(authentication(authentication))
            .with(csrf())
            .param("weatherId", weatherId.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn();

    List<List<ClothesDto>> lists = objectMapper.readValue(response.getResponse().getContentAsString(), List.class);
    assertEquals(1, lists.size());
  }

}
