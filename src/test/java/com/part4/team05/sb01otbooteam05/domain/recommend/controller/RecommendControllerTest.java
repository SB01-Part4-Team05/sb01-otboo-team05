package com.part4.team05.sb01otbooteam05.domain.recommend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesDto;
import com.part4.team05.sb01otbooteam05.domain.recommend.service.RecommendService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
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


  @Test
  @WithMockUser
  void getRecommendSet() throws Exception {
    UUID weatherId = UUID.randomUUID();
    List<List<ClothesDto>> result = new ArrayList<>();
    result.add(List.of(mock(ClothesDto.class)));

    given(recommendService.getRecommend(any(UUID.class),any(UUID.class)))
        .willReturn(result);

    MvcResult response = mockMvc.perform(MockMvcRequestBuilders.get("/api/recommendations")
        .with(csrf())
        .param("weatherId", String.valueOf(weatherId))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn();

    List<List<ClothesDto>> lists = objectMapper.readValue(response.getResponse().getContentAsString(),List.class);

    assertEquals(1,lists.size());

  }
}