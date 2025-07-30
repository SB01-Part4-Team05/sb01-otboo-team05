package com.part4.team05.sb01otbooteam05.domain.recommend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeValue;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeDefinition;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesDto;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.ClothesType;
import com.part4.team05.sb01otbooteam05.domain.clothes.mapper.ClothesMapper;
import com.part4.team05.sb01otbooteam05.domain.clothes.service.ClothesService;
import com.part4.team05.sb01otbooteam05.domain.recommend.dto.RecommendationDto;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import com.part4.team05.sb01otbooteam05.domain.weather.service.WeatherService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

  @BeforeEach
  void setup() {
    recommendService.makeCriteria();
    recommendService.makeWeatherCriteria();
  }

  @Test
  void getRecommend_success() {
    UUID ownerId = UUID.randomUUID();
    UUID weatherId = UUID.randomUUID();

    Clothes top = createClothesWithThickness(ClothesType.TOP, "THICK");
    Clothes bottom = createClothesWithThickness(ClothesType.BOTTOM, "THIN");
    Clothes acc = createClothesWithThickness(ClothesType.ACC, "SLIMTHIN");
    Clothes outer = createClothesWithThickness(ClothesType.OUTER, "SLIMTHICK");

    when(clothesService.findAllByOwnerId(ownerId))
        .thenReturn(List.of(top, bottom, acc, outer));

    Weather weather = mock(Weather.class);
    when(weather.getTemperatureMax()).thenReturn(20.0);
    when(weather.getTemperatureMin()).thenReturn(10.0);
    when(weatherService.getWeatherEntityByIdOrThrow(weatherId)).thenReturn(weather);

    RecommendationDto result = recommendService.getRecommend(ownerId, weatherId);

    assertNotNull(result);
    assertEquals(ownerId, result.userId());
    assertEquals(weatherId, result.weatherId());
  }


  private Clothes createClothesWithThickness(ClothesType type, String thicknessValue) {
    Clothes clothes = new Clothes();
    clothes.setType(type);
    clothes.setName("Test " + type);

    AttributeDefinition thicknessDef = mock(AttributeDefinition.class);

    AttributeValue thicknessAttr = mock(AttributeValue.class);

    clothes.setAttributeValues(List.of(thicknessAttr));
    return clothes;
  }

}
