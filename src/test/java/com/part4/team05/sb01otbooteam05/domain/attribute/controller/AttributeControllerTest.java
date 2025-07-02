package com.part4.team05.sb01otbooteam05.domain.attribute.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team05.sb01otbooteam05.domain.attribute.service.AttributeService;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesAttributeDefCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester.MockMvcRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(controllers = AttributeController.class)
@ExtendWith(MockitoExtension.class)
class AttributeControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockitoBean
  AttributeService attributeService;

//  @Test
//  @WithMockUser(roles = "ADMIN")
//  void createDef() throws Exception{
//    ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest("test1",
//        List.of("a","b","c"));
//
//    mockMvc.perform(MockMvcRequestBuilders.post("/api/clothes/attribute-defs")
//            .content(objectMapper.writeValueAsString(request))
//            .contentType(MediaType.APPLICATION_JSON))
//        .andExpect(status().isCreated());
//  }

  @Test
  void update() {
  }

  @Test
  void deleteDef() {
  }

  @Test
  void getDef() {
  }
}