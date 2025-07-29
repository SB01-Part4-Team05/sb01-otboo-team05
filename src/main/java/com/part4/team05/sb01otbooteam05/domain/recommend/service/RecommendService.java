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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {
  private final ClothesService clothesService;
  private final ClothesMapper clothesMapper;
  private final WeatherService weatherService;

  private final Random random = new Random();
  private final Map<ThicknessType,Integer> criteria = new HashMap<>();
  private final Map<Integer, Integer> weatherCriteria = new HashMap<>();

  public List<List<ClothesDto>> getRecommend(@NotNull UUID ownerId, @NotNull UUID weatherId) {
    Map<StyleType, Map<ClothesType, List<Clothes>>> map = getMap(ownerId);
    List<List<Clothes>> result = new ArrayList<>();
    int weatherValue = getWeatherValue(weatherId);

    for (StyleType styleType : map.keySet()) {
      Map<ClothesType, List<Clothes>> innerMap = map.get(styleType);
      Map<Clothes, List<Integer>> scoreMap;

      List<Clothes> tops = new ArrayList<>(innerMap.getOrDefault(ClothesType.TOP, Collections.emptyList()));
      List<Clothes> bottoms = new ArrayList<>(innerMap.getOrDefault(ClothesType.BOTTOM, Collections.emptyList()));
      List<Clothes> acc = new ArrayList<>(innerMap.getOrDefault(ClothesType.ACC, Collections.emptyList()));
      acc.addAll(innerMap.getOrDefault(ClothesType.CAP, Collections.emptyList()));
      acc.addAll(innerMap.getOrDefault(ClothesType.BAG, Collections.emptyList()));
      acc.addAll(innerMap.getOrDefault(ClothesType.SCARF, Collections.emptyList()));
      acc.addAll(innerMap.getOrDefault(ClothesType.SHOES, Collections.emptyList()));
      acc.addAll(innerMap.getOrDefault(ClothesType.SOCKS, Collections.emptyList()));
      List<Clothes> outers = new ArrayList<>(innerMap.getOrDefault(ClothesType.OUTER, Collections.emptyList()));

      scoreMap = getScore(tops, bottoms, weatherValue);
      List<List<Clothes>> settings = getSetting(scoreMap, bottoms, outers);

      settings.addAll(getDressScore(innerMap.getOrDefault(ClothesType.DRESS, Collections.emptyList()), outers, weatherValue));

      int time = 0;
      while (time < 3 && !settings.isEmpty() && !acc.isEmpty()) {
        int indexList = random.nextInt(settings.size());
        int indAcc = random.nextInt(acc.size());

        List<Clothes> setting = new ArrayList<>(settings.get(indexList));
        setting.add(acc.get(indAcc));
        result.add(setting);
        time++;
      }

      result.addAll(settings);
    }

    List<List<ClothesDto>> finalResult = result.stream()
        .map(clothes -> {
          List<ClothesDto> dtoList = clothesMapper.toDtoList(clothes);
          return dtoList != null ? dtoList : Collections.<ClothesDto>emptyList();
        })
        .filter(list -> !list.isEmpty())
        .collect(Collectors.toList());

    try {
      String url = "http://recommend-flask:5000/rank";

      RestTemplate restTemplate = new RestTemplate();
      ObjectMapper objectMapper = new ObjectMapper();

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(finalResult), headers);

      ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

      if (response.getStatusCode() == HttpStatus.OK) {
        String responseBody = response.getBody();
        if (responseBody != null && !responseBody.isBlank()) {
          List<List<ClothesDto>> aiResult = objectMapper.readValue(responseBody,
              objectMapper.getTypeFactory().constructCollectionType(List.class,
                  objectMapper.getTypeFactory().constructCollectionType(List.class, ClothesDto.class)));

          if (aiResult != null) {
            return aiResult.stream()
                .map(list -> list != null ? list : Collections.<ClothesDto>emptyList())
                .collect(Collectors.toList());
          }
        }
      }
    } catch (Exception e) {
      if (e instanceof JsonProcessingException) {
                log.warn("AI 서비스 응답 파싱 실패: {}", e.getMessage());
      } else if (e instanceof ResourceAccessException) {
        log.warn("AI 서비스 연결 실패: {}", e.getMessage());
      } else {
        log.error("AI 서비스 호출 중 예상치 못한 오류 발생", e);
      }
    }

    return finalResult != null ? finalResult : Collections.emptyList();
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
        .orElseThrow(() -> new NoSuchElementException("No weather weight found for temperature: " + mid));
  }

  private List<List<Clothes>> getDressScore(List<Clothes> dresses, List<Clothes> outers, int weatherValue) {
    List<List<Clothes>> list = new ArrayList<>();

    for (Clothes dress : dresses) {
      int totalScore = Math.abs(getWeight(dress) - weatherValue);

      if (totalScore <= 5) {
        list.add(List.of(dress));
      } else {
        List<Clothes> canWear = withOuter(totalScore, outers);
        for (Clothes outer : canWear) {
          list.add(List.of(dress, outer));
        }
      }
    }

    return list;
  }

  private List<List<Clothes>> getSetting(Map<Clothes, List<Integer>> map, List<Clothes> bottoms, List<Clothes> outers) {
    List<List<Clothes>> result = new ArrayList<>();

    for (Clothes top : map.keySet()) {
      List<Integer> scores = map.get(top);
      for (int i = 0; i < scores.size(); i++) {
        int score = scores.get(i);
        if (i >= bottoms.size()) continue;
        Clothes bottom = bottoms.get(i);

        if (score >= 0 && score <= 5) {
          result.add(List.of(top, bottom));
        } else {
          List<Clothes> outersFit = withOuter(score, outers);
          for (Clothes outer : outersFit) {
            result.add(List.of(top, bottom, outer));
          }
        }
      }
    }

    return result;
  }

  private List<Clothes> withOuter(int totalScore, List<Clothes> outers) {
    return outers.stream()
        .filter(outer -> {
          int outerScore = getWeight(outer);
          int combined = totalScore + outerScore;
          return combined >= 0 && combined <= 5;
        })
        .collect(Collectors.toList());
  }

  private Map<Clothes, List<Integer>> getScore(List<Clothes> tops, List<Clothes> bottoms, int weatherValue) {
    Map<Clothes, List<Integer>> map = new HashMap<>();

    for (Clothes top : tops) {
      int topScore = getWeight(top);
      List<Integer> scores = new ArrayList<>();
      for (Clothes bottom : bottoms) {
        int bottomScore = getWeight(bottom);
        scores.add(-Math.abs(topScore - bottomScore - weatherValue) - (topScore - bottomScore));
      }
      map.put(top, scores);
    }

    return map;
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
