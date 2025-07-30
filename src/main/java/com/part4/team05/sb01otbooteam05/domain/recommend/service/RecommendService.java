package com.part4.team05.sb01otbooteam05.domain.recommend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.StyleType;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.ThicknessType;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesDto;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.ClothesType;
import com.part4.team05.sb01otbooteam05.domain.clothes.mapper.ClothesMapper;
import com.part4.team05.sb01otbooteam05.domain.clothes.service.ClothesService;
import com.part4.team05.sb01otbooteam05.domain.recommend.dto.RecommendationiDto;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import com.part4.team05.sb01otbooteam05.domain.weather.service.WeatherService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendService {
  private final ClothesService clothesService;
  private final ClothesMapper clothesMapper;
  private final WeatherService weatherService;

  private final Random random = new Random();
  private final Map<ThicknessType,Integer> criteria = new HashMap<>();
  private final Map<Integer, Integer> weatherCriteria = new HashMap<>();


  public RecommendationiDto getRecommend(@NotNull UUID ownerId, @NotNull UUID weatherId) {
    Map<StyleType, Map<ClothesType, List<Clothes>>> styleMap = getMap(ownerId);

    if (styleMap.isEmpty()) {
      log.info("사용자의 옷장이 비어있습니다. 빈 추천을 반환합니다. ownerId={}", ownerId);
      return new RecommendationiDto(weatherId, ownerId, Collections.emptyList());
    }

    int weatherValue = getWeatherValue(weatherId);
    List<List<Clothes>> allSettings = new ArrayList<>();

    for (StyleType style : styleMap.keySet()) {
      Map<ClothesType, List<Clothes>> inner = styleMap.get(style);

      List<Clothes> tops = new ArrayList<>(inner.getOrDefault(ClothesType.TOP, Collections.emptyList()));
      List<Clothes> bottoms = new ArrayList<>(inner.getOrDefault(ClothesType.BOTTOM, Collections.emptyList()));
      List<Clothes> dresses = new ArrayList<>(inner.getOrDefault(ClothesType.DRESS, Collections.emptyList()));
      List<Clothes> outers = new ArrayList<>(inner.getOrDefault(ClothesType.OUTER, Collections.emptyList()));

      List<Clothes> acc = new ArrayList<>();
      acc.addAll(inner.getOrDefault(ClothesType.CAP, Collections.emptyList()));
      acc.addAll(inner.getOrDefault(ClothesType.BAG, Collections.emptyList()));
      acc.addAll(inner.getOrDefault(ClothesType.SCARF, Collections.emptyList()));
      acc.addAll(inner.getOrDefault(ClothesType.SHOES, Collections.emptyList()));
      acc.addAll(inner.getOrDefault(ClothesType.SOCKS, Collections.emptyList()));
      acc.addAll(inner.getOrDefault(ClothesType.ACC, Collections.emptyList()));

      List<List<Clothes>> baseSettings = getBaseSettings(tops, bottoms, weatherValue);
      List<List<Clothes>> dressSettings = getDressSettings(dresses, weatherValue);
      List<List<Clothes>> allBase = new ArrayList<>();
      allBase.addAll(baseSettings);
      allBase.addAll(dressSettings);

      List<List<Clothes>> extendedSettings = new ArrayList<>();

      for (List<Clothes> setting : allBase) {
        int warmth = setting.stream().mapToInt(this::getWeight).sum();
        List<Clothes> suitableOuters = findSuitableOuters(outers, warmth, weatherValue);

        if (suitableOuters.isEmpty()) {
          extendedSettings.add(setting);
        } else {
          for (Clothes outer : suitableOuters) {
            List<Clothes> extended = new ArrayList<>(setting);
            extended.add(outer);
            extendedSettings.add(extended);
          }
        }
      }

      List<List<Clothes>> finalWithAcc = new ArrayList<>();
      int count = 0;
      while (count < 3 && !extendedSettings.isEmpty()) {
        List<Clothes> base = extendedSettings.get(random.nextInt(extendedSettings.size()));
        List<Clothes> full = new ArrayList<>(base);
        if (!acc.isEmpty()) {
          full.add(acc.get(random.nextInt(acc.size())));
        }
        finalWithAcc.add(full);
        count++;
      }

      allSettings.addAll(finalWithAcc);
    }

    List<List<ClothesDto>> dtoResult = allSettings.stream()
        .map(clothes -> {
          List<ClothesDto> dtoList = clothesMapper.toDtoList(clothes);
          if (dtoList == null) return Collections.<ClothesDto>emptyList();
          dtoList.forEach(dto -> {
            if (dto.getAttributes() == null) dto.setAttributes(new ArrayList<>());
          });
          return dtoList;
        })
        .filter(list -> !list.isEmpty())
        .collect(Collectors.toList());

    try {
      String url = "http://54.180.115.86:5000/rank";
      RestTemplate restTemplate = new RestTemplate();
      ObjectMapper objectMapper = new ObjectMapper();

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(dtoResult), headers);

      ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

      if (response.getStatusCode() == HttpStatus.OK) {
        String body = response.getBody();
        if (body != null && !body.isBlank()) {
          List<List<ClothesDto>> aiList = objectMapper.readValue(body,
              objectMapper.getTypeFactory().constructCollectionType(List.class,
                  objectMapper.getTypeFactory().constructCollectionType(List.class, ClothesDto.class)));

          if (aiList != null) {
            return new RecommendationiDto(weatherId, ownerId, aiList);
          }
        }
      }
    } catch (Exception e) {
      log.warn("AI 서버 호출 실패: {}", e.getMessage());
    }

    return new RecommendationiDto(weatherId, ownerId, dtoResult);
  }

  private Map<StyleType, Map<ClothesType, List<Clothes>>> getMap(UUID ownerId) {
    List<Clothes> allClothes = clothesService.findAllByOwnerId(ownerId);

    return allClothes.stream()
        .filter(clothes -> clothes.getAttributeValues().stream()
            .anyMatch(av -> av.getDefinition() != null && "style".equals(av.getDefinition().getName())))
        .collect(Collectors.groupingBy(
            clothes -> clothes.getAttributeValues().stream()
                .filter(av -> av.getDefinition() != null && "style".equals(av.getDefinition().getName()))
                .map(av -> {
                  try {
                    return StyleType.valueOf(av.getValue());
                  } catch (Exception e) {
                    return StyleType.CASUAL;
                  }
                })
                .findFirst()
                .orElse(StyleType.CASUAL),
            Collectors.groupingBy(Clothes::getType)
        ));
  }

  private List<List<Clothes>> getBaseSettings(List<Clothes> tops, List<Clothes> bottoms, int weatherValue) {
    List<List<Clothes>> result = new ArrayList<>();
    for (Clothes top : tops) {
      int topScore = getWeight(top);
      for (Clothes bottom : bottoms) {
        int bottomScore = getWeight(bottom);
        int warmth = topScore + bottomScore;
        if (Math.abs(warmth - weatherValue) <= 5) {
          result.add(List.of(top, bottom));
        }
      }
    }
    return result;
  }

  private List<List<Clothes>> getDressSettings(List<Clothes> dresses, int weatherValue) {
    List<List<Clothes>> list = new ArrayList<>();
    for (Clothes dress : dresses) {
      int warmth = getWeight(dress);
      if (Math.abs(warmth - weatherValue) <= 5) {
        list.add(List.of(dress));
      }
    }
    return list;
  }

  private List<Clothes> findSuitableOuters(List<Clothes> outers, int currentWarmth, int weatherValue) {
    return outers.stream()
        .filter(outer -> {
          int outerScore = getWeight(outer);
          int total = currentWarmth + outerScore;
          return Math.abs(total - weatherValue) <= 5;
        })
        .collect(Collectors.toList());
  }

  private int getWeight(Clothes clothes) {
    return clothes.getAttributeValues().stream()
        .filter(attributeValue -> attributeValue.getDefinition() != null &&
            "thickness".equals(attributeValue.getDefinition().getName()))
        .map(attributeValue -> {
          try {
            return ThicknessType.valueOf(attributeValue.getValue());
          } catch (IllegalArgumentException | NullPointerException e) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .map(thickness -> criteria.getOrDefault(thickness, 0))
        .findFirst()
        .orElse(0);
  }

  private int getWeatherValue(UUID weatherId) {
    Weather weather = weatherService.getWeatherEntityByIdOrThrow(weatherId);
    double mid = (weather.getTemperatureMax() + weather.getTemperatureMin()) / 2;

    return weatherCriteria.entrySet().stream()
        .filter(entry -> mid < entry.getKey() + 5 && mid >= entry.getKey())
        .map(Map.Entry::getValue)
        .findFirst()
        .orElse(0);
  }

  @PostConstruct
  public void makeCriteria() {
    criteria.put(ThicknessType.THICK, 5);
    criteria.put(ThicknessType.SLIMTHICK, 2);
    criteria.put(ThicknessType.SLIMTHIN, -2);
    criteria.put(ThicknessType.THIN, -5);
  }

  @PostConstruct
  public void makeWeatherCriteria() {
    weatherCriteria.put(-15, -15);
    weatherCriteria.put(-10, -10);
    weatherCriteria.put(-5, -7);
    weatherCriteria.put(0, -5);
    weatherCriteria.put(5, 2);
    weatherCriteria.put(10, 5);
    weatherCriteria.put(15, 7);
    weatherCriteria.put(20, 10);
    weatherCriteria.put(25, 12);
    weatherCriteria.put(30, 15);
  }





  /*
  추천 기준
     날씨에서 가져올 것
     온도 최고 / 최저
     온도 미디언 값 -> 가중치 확인
     온도 가중치는 5도 단위로 끊고 -15 ~ 35 까지
     -15 / -10 / -7 / -5 / 2 / 5 / 7 / 10 / 12 / 15
     옷 가중치 + 온도 가중치가 0 ~ 5 사이가 되도록 하는 것이 추천의 목표
   */
}
