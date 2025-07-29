package com.part4.team05.sb01otbooteam05.domain.recommend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.part4.team05.sb01otbooteam05.config.SecurityConfig;
import com.part4.team05.sb01otbooteam05.domain.auth.security.jwt.JwtTokenProvider;
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
  void getRecommend() {
    UUID ownerId = UUID.randomUUID();
    UUID weatherId = UUID.randomUUID();

    Clothes top = mockClothes(ClothesType.TOP);
    Clothes bottom = mockClothes(ClothesType.BOTTOM);
    Clothes acc = mockClothes(ClothesType.ACC);
    Clothes outer = mockClothes(ClothesType.OUTER);

    when(clothesService.findAllByOwnerId(ownerId))
        .thenReturn(List.of(top, bottom, acc, outer));

    try {
      RecommendationiDto result = recommendService.getRecommend(ownerId, weatherId);

      assertNotNull(result);
      assertFalse(result.clothes().isEmpty());
      assertEquals("TOP", result.clothes().get(0).get(0).getType());
    } catch (Exception e) {
      System.out.println( e.getMessage());
    }
  }

  private Clothes mockClothes(ClothesType type) {
    Clothes clothes = mock(Clothes.class);
    clothes.setType(type);

    return clothes;
  }
}

