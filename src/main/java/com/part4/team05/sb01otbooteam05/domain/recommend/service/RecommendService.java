package com.part4.team05.sb01otbooteam05.domain.recommend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.StyleType;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.ThicknessType;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesDto;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.ClothesType;
import com.part4.team05.sb01otbooteam05.domain.clothes.mapper.ClothesMapper;
import com.part4.team05.sb01otbooteam05.domain.clothes.service.ClothesService;
import com.part4.team05.sb01otbooteam05.domain.recommend.dto.RecommendClothesDto;
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
  private final Map<String,Integer> criteria = new HashMap<>();
  private final Map<Integer, Integer> weatherCriteria = new HashMap<>();

  public RecommendationDto getRecommend(@NotNull UUID ownerId, @NotNull UUID weatherId) {
    log.info("추천 요청 - ownerId: {}, weatherId: {}", ownerId, weatherId);

    int weatherValue = getWeatherValue(weatherId);
    log.info("계산된 weatherValue: {}", weatherValue);

    List<Clothes> allClothes = clothesService.findAllByOwnerId(ownerId);
    log.info("조회된 전체 의류 개수: {}", allClothes.size());

    List<Clothes> tops = filterAndSort(allClothes, ClothesType.TOP, weatherValue);
    List<Clothes> bottoms = filterAndSort(allClothes, ClothesType.BOTTOM, weatherValue);
    List<Clothes> dresses = filterAndSort(allClothes, ClothesType.DRESS, weatherValue);
    List<Clothes> outers = filterAndSort(allClothes, ClothesType.OUTER, weatherValue);
    List<Clothes> accessories = filterAndSort(allClothes, ClothesType.ACC, weatherValue);

    log.info("추천된 옷 개수 - tops: {}, bottoms: {}, dresses: {}, outers: {}, accessories: {}",
        tops.size(), bottoms.size(), dresses.size(), outers.size(), accessories.size());

    List<List<ClothesDto>> dtoLists = new ArrayList<>();
    dtoLists.add(tops.stream().map(clothesMapper::toDto).toList());
    dtoLists.add(bottoms.stream().map(clothesMapper::toDto).toList());
    dtoLists.add(dresses.stream().map(clothesMapper::toDto).toList());
    dtoLists.add(outers.stream().map(clothesMapper::toDto).toList());
    dtoLists.add(accessories.stream().map(clothesMapper::toDto).toList());

    List<ClothesDto> recommended = callAIServer(dtoLists);

    List<UUID> recommendedIds = recommended.stream()
        .map(ClothesDto::getId)
        .toList();

    List<ClothesDto> others = dtoLists.stream()
        .flatMap(List::stream)
        .filter(c -> !recommendedIds.contains(c.getId()))
        .toList();

    List<ClothesDto> finalResult = new ArrayList<>();
    finalResult.addAll(recommended);
    finalResult.addAll(others);

    List<RecommendClothesDto> convertedResult = finalResult.stream()
        .filter(Objects::nonNull)
        .map(RecommendClothesDto::from)
        .toList();

    return new RecommendationDto(weatherId, ownerId, convertedResult);
  }

  private List<Clothes> filterAndSort(List<Clothes> clothesList, ClothesType type, int weatherValue) {
    List<Clothes> filtered = clothesList.stream()
        .filter(c -> c.getType() == type)
        .sorted(Comparator.comparingInt(c -> Math.abs(getWeight(c) + weatherValue)))
        .limit(5)
        .toList();

    log.debug("filterAndSort - type: {}, 결과 개수: {}", type, filtered.size());
    return filtered;
  }

  private int getWeight(Clothes clothes) {
    return clothes.getAttributeValues().stream()
        .filter(attributeValue -> attributeValue.getDefinition() != null &&
            "thickness".equalsIgnoreCase(attributeValue.getDefinition().getName()))
        .map(attributeValue -> {
          try {
            return ThicknessType.valueOf(attributeValue.getValue().toUpperCase());
          } catch (IllegalArgumentException | NullPointerException e) {
            log.debug("getWeight - ThicknessType 변환 실패: {}", attributeValue.getValue());
            return null;
          }
        })
        .filter(Objects::nonNull)
        .mapToInt(thickness -> criteria.getOrDefault(thickness, 0))
        .findFirst()
        .orElse(0);
  }


  private int getWeatherValue(UUID weatherId) {
    Weather weather = weatherService.getWeatherEntityByIdOrThrow(weatherId);
    double mid = (weather.getTemperatureMax() + weather.getTemperatureMin()) / 2.0;
    log.info("getWeatherValue - weatherId: {}, temperatureMid: {}", weatherId, mid);

    int value = weatherCriteria.entrySet().stream()
        .filter(entry -> mid < entry.getKey() + 5 && mid >= entry.getKey())
        .map(Map.Entry::getValue)
        .findFirst()
        .orElse(0);

    log.info("getWeatherValue - mapped weatherValue: {}", value);
    return value;
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

      if (response.getBody() != null) {
        for (ClothesDto[] arr : response.getBody()) {
          if (arr.length > 0) {
            log.info("AI 서버 추천 응답: {}", mapper.writeValueAsString(arr));
            return List.of(arr);
          }
        }
      }
    } catch (Exception e) {
      log.warn("AI 서버 호출 실패: {}", e.getMessage());
    }

    return combos.stream()
        .filter(list -> !list.isEmpty())
        .findFirst()
        .orElse(List.of());
  }


  @PostConstruct
  public void makeCriteria() {
    criteria.put(ThicknessType.THICK.toString(), 15);
    criteria.put(ThicknessType.SLIMTHICK.toString(), 10);
    criteria.put(ThicknessType.SLIMTHIN.toString(), -10);
    criteria.put(ThicknessType.THIN.toString(), -15);
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
    weatherCriteria.put(35, 20);
  }
}
