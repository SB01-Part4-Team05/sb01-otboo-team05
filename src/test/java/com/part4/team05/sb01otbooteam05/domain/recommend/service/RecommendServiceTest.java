package com.part4.team05.sb01otbooteam05.domain.recommend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.part4.team05.sb01otbooteam05.config.SecurityConfig;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeValue;
import com.part4.team05.sb01otbooteam05.domain.auth.security.jwt.JwtTokenProvider;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesAttributeDto;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesDto;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.ClothesType;
import com.part4.team05.sb01otbooteam05.domain.clothes.mapper.ClothesMapper;
import com.part4.team05.sb01otbooteam05.domain.clothes.service.ClothesService;
import com.part4.team05.sb01otbooteam05.domain.recommend.dto.RecommendationiDto;
import com.part4.team05.sb01otbooteam05.domain.weather.service.WeatherService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;

@ExtendWith(MockitoExtension.class)
class RecommendServiceTest {

  @InjectMocks
  RecommendService recommendService;

  @Mock
  ClothesService clothesService;

  @Mock
  ClothesMapper clothesMapper;

  @Mock
  WeatherService weatherService;

  @Mock
  JwtTokenProvider jwtTokenProvider;

  @Test
  void getRecommend_success() {
    UUID ownerId = UUID.randomUUID();
    UUID weatherId = UUID.randomUUID();

    Clothes top = mockClothesWithAttributes(ClothesType.TOP);
    Clothes bottom = mockClothesWithAttributes(ClothesType.BOTTOM);
    Clothes acc = mockClothesWithAttributes(ClothesType.ACC);
    Clothes outer = mockClothesWithAttributes(ClothesType.OUTER);

    when(clothesService.findAllByOwnerId(ownerId))
        .thenReturn(List.of(top, bottom, acc, outer));

    RecommendationiDto result = recommendService.getRecommend(ownerId, weatherId);

    assertNotNull(result);
  }

  private Clothes mockClothesWithAttributes(ClothesType type) {
    Clothes clothes = new Clothes();
    clothes.setType(type);
    clothes.setName("Test " + type);
    clothes.setImageUrl("http://example.com/test.jpg");
    clothes.setAttributeValues(List.of(mock(AttributeValue.class)));
    return clothes;
  }

}

