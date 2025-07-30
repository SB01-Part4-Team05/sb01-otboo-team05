package com.part4.team05.sb01otbooteam05.domain.recommend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.StyleType;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.ThicknessType;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesDto;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.ClothesType;
import com.part4.team05.sb01otbooteam05.domain.clothes.mapper.ClothesMapper;
import com.part4.team05.sb01otbooteam05.domain.clothes.service.ClothesService;
import com.part4.team05.sb01otbooteam05.domain.recommend.dto.RecommendationDto;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import com.part4.team05.sb01otbooteam05.domain.weather.service.WeatherService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  public RecommendationDto getRecommend(@NotNull UUID ownerId, @NotNull UUID weatherId) {
    int weatherValue = getWeatherValue(weatherId);
    List<Clothes> allClothes = clothesService.findAllByOwnerId(ownerId);

    List<Clothes> tops = filterAndSort(allClothes, ClothesType.TOP, weatherValue);
    List<Clothes> bottoms = filterAndSort(allClothes, ClothesType.BOTTOM, weatherValue);
    List<Clothes> dresses = filterAndSort(allClothes, ClothesType.DRESS, weatherValue);
    List<Clothes> outers = filterAndSort(allClothes, ClothesType.OUTER, weatherValue);
    List<Clothes> accessories = filterAndSort(allClothes, ClothesType.ACC, weatherValue);

    List<List<Clothes>> outfitCombos = new ArrayList<>();
    outfitCombos.addAll(getBaseSettings(tops, bottoms, weatherValue)); // top + bottom
    outfitCombos.addAll(getDressSettings(dresses, weatherValue)); // dress only

    // 아우터 추가
    List<List<Clothes>> withOuters = new ArrayList<>();
    for (List<Clothes> combo : outfitCombos) {
      int currentWarmth = combo.stream().mapToInt(this::getWeight).sum();
      List<Clothes> suitableOuters = findSuitableOuters(outers, currentWarmth, weatherValue);
      if (!suitableOuters.isEmpty()) {
        for (Clothes outer : suitableOuters) {
          List<Clothes> newCombo = new ArrayList<>(combo);
          newCombo.add(outer);
          withOuters.add(newCombo);
        }
      } else {
        withOuters.add(combo);
      }
    }
    List<List<Clothes>> finalCombos = new ArrayList<>();
    for (List<Clothes> combo : withOuters) {
      List<Clothes> newCombo = new ArrayList<>(combo);
      accessories.stream().limit(1).forEach(newCombo::add);
      finalCombos.add(newCombo);
    }

    // Clothes → ClothesDto
    List<List<ClothesDto>> dtoCombos = finalCombos.stream()
        .map(list -> list.stream().map(clothesMapper::toDto).toList())
        .toList();

    // AI 서버 호출
    List<ClothesDto> recommended = callAIServer(dtoCombos);

    return new RecommendationDto(weatherId, ownerId, recommended);
  }

  private List<Clothes> filterAndSort(List<Clothes> clothesList, ClothesType type, int weatherValue) {
    return clothesList.stream()
        .filter(c -> c.getType() == type)
        .sorted(Comparator.comparingInt(c -> Math.abs(getWeight(c) - weatherValue)))
        .limit(5) // 필요에 따라 조정
        .toList();
  }

  private List<ClothesDto> callAIServer(List<List<ClothesDto>> combos) {
    try {
      String url = "http://54.180.115.86:5000/rank";
      ObjectMapper mapper = new ObjectMapper();

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      String requestBody = mapper.writeValueAsString(combos);
      HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<ClothesDto[][]> response =
          restTemplate.postForEntity(url, request, ClothesDto[][].class);

      if (response.getBody() != null && response.getBody().length > 0) {
        return List.of(response.getBody()[0]); // AI가 정한 첫 번째 조합만 반환
      }
    } catch (Exception e) {
      log.warn("AI 서버 호출 실패: {}", e.getMessage());
    }

    return combos.isEmpty() ? List.of() : combos.get(0); // 실패 시 첫 번째 후보 반환
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
    double mid = (weather.getTemperatureMax() + weather.getTemperatureMin()) / 2.0;

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
}
