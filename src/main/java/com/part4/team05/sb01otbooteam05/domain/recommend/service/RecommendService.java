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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class RecommendService {
  private final ClothesService clothesService;
  private final ClothesMapper clothesMapper;
  private final WeatherService weatherService;

  private final Random random = new Random();
  private final Map<ThicknessType,Integer> criteria = new HashMap<>(); // 옷 두께 가중치
  private final Map<Integer, Integer> weatherCriteria = new HashMap<>();

  // 온도에 따라 옷 스타일 별 추천
  public List<List<ClothesDto>> getRecommend(@NotNull UUID ownerId, @NotNull UUID weatherId){
    Map<StyleType, Map<ClothesType,List<Clothes>>> map = getMap(ownerId);
    List<List<Clothes>> result = new ArrayList<>();
    int weatherValue = getWeatherValue(weatherId);
    for(StyleType styleType : map.keySet()){
      Map<ClothesType,List<Clothes>> innerMap = map.get(styleType);
      Map<Clothes, List<Integer>> scoreMap;

      List<Clothes> tops = innerMap.getOrDefault(ClothesType.TOP,Collections.emptyList());
      List<Clothes> bottoms = innerMap.getOrDefault(ClothesType.BOTTOM,Collections.emptyList());
      List<Clothes> acc = innerMap.getOrDefault(ClothesType.ACC,Collections.emptyList());
      acc.addAll(innerMap.getOrDefault(ClothesType.CAP,Collections.emptyList()));
      acc.addAll(innerMap.getOrDefault(ClothesType.BAG,Collections.emptyList()));
      acc.addAll(innerMap.getOrDefault(ClothesType.SCARF,Collections.emptyList()));
      acc.addAll(innerMap.getOrDefault(ClothesType.SHOES,Collections.emptyList()));
      acc.addAll(innerMap.getOrDefault(ClothesType.SOCKS,Collections.emptyList()));
      List<Clothes> outers = innerMap.getOrDefault(ClothesType.OUTER,Collections.emptyList());

      scoreMap = getScore(tops,bottoms,weatherValue);
      List<List<Clothes>> settings = getSetting(scoreMap,bottoms,outers);

      settings.addAll(getDressScore(innerMap.getOrDefault(ClothesType.DRESS,Collections.emptyList()),
          outers,weatherValue));

      int time = 0;

      while(time < 3 && !settings.isEmpty() && !acc.isEmpty()){
        int indexList = random.nextInt(settings.size());

        int indAcc = random.nextInt(acc.size());
        List<Clothes> setting = new ArrayList<>(settings.get(indexList));
        setting.add(acc.get(indAcc));

        result.add(setting);
        time++;
      }

      result.addAll(settings);
    }

    List<List<ClothesDto>> finalResult = new ArrayList<>();

    for(List<Clothes> list : result){
      finalResult.add(clothesMapper.toDtoList(list));
    }

    try{
      String url = "http://localhost:8000/rank";

      RestTemplate restTemplate = new RestTemplate();
      ObjectMapper objectMapper = new ObjectMapper();

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(finalResult),headers);

      ResponseEntity<String> response = restTemplate.postForEntity(url,request,String.class);

      if (response.getStatusCode() == HttpStatus.OK) {
        return objectMapper.readValue(response.getBody(),
            objectMapper.getTypeFactory().constructCollectionType(List.class,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ClothesDto.class)));
      }

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    return finalResult;
  }

  // 원피스 가중치 점수
  private List<List<Clothes>> getDressScore(List<Clothes> dresses,List<Clothes> outers, int weatherValue) {
    List<List<Clothes>> list = new ArrayList<>();

    for(Clothes dress : dresses){
      int totalScore = Math.abs(getWeight(dress)-weatherValue);

      if(totalScore <= 5 && totalScore >= 0){
        list.add(List.of(dress));
      }
      else{
        List<Clothes> canWear = withOuter(totalScore, outers);
        for (Clothes outer : canWear) {
          list.add(List.of(dress, outer));
        }
      }
    }

    return list;
  }

  // 유저가 가진 옷을 스타일별로 분류한 뒤 다시 의상 타입별로 분류한 맵 생성
  private Map<StyleType, Map<ClothesType,List<Clothes>>> getMap(UUID ownerId){
    List<Clothes> allClothes = clothesService.findAllByOwnerId(ownerId);


    return allClothes.stream()
        .collect(Collectors.groupingBy(
            clothes -> clothes.getAttributeValues().stream()
                .filter(av -> av.getDefinition().getName().equals("style"))
                .map(av -> StyleType.valueOf(av.getValue()))
                .findFirst()
                .orElseThrow(NoSuchElementException::new),
            Collectors.groupingBy(Clothes::getType)
        ));
  }

  // 가중치 계산
  private int getWeight(Clothes clothes){
    return clothes.getAttributeValues().stream()
        .filter(attributeValue -> "thickness".equals(attributeValue.getDefinition().getName()))
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

  // 옷에 부여하는 기준 가중치. 서비스 시작 시에 생성됨
  @PostConstruct
  public void makeCriteria(){
    criteria.put(ThicknessType.THICK, 5);
    criteria.put(ThicknessType.SLIMTHICK, 2);
    criteria.put(ThicknessType.SLIMTHIN,-2);
    criteria.put(ThicknessType.THIN,-5);
  }

  // 상+하의 조합별 가중치 점수
  private Map<Clothes,List<Integer>> getScore(List<Clothes> tops, List<Clothes> bottoms, int weatherValue){
    Map<Clothes,List<Integer>> map = new HashMap<>();

    for(Clothes top : tops){
      int topScore = getWeight(top);
      List<Integer> scores = new ArrayList<>();
      for(Clothes bottom : bottoms){
        int bottomScore = getWeight(bottom);
        scores.add(-Math.abs(topScore-bottomScore - weatherValue) - (topScore-bottomScore));
      }
      map.put(top,scores);
    }

    return map;
  }

  // 추천 세트 리스트
  private List<List<Clothes>>  getSetting(Map<Clothes,List<Integer>> map, List<Clothes> bottoms
  ,List<Clothes> outers){
    List<List<Clothes>> result = new ArrayList<>();

    for(Clothes clothes : map.keySet()){
      for(int i = 0 ; i < map.get(clothes).size() ; i++){
        int totalScore = map.get(clothes).get(i);
        if(totalScore <= 5 && totalScore >= 0){
          result.add(List.of(clothes,bottoms.get(i)));
        }
        else{
          List<Clothes> canWear = withOuter(totalScore,outers);
          for(Clothes c : canWear){
            result.add(List.of(clothes,bottoms.get(i),c));
          }
        }
      }
    }

    return result;
  }

  // 상+하의 나 원피스와 아우터를 같이 추천하기 위함
  private List<Clothes> withOuter(int totalScore, List<Clothes> outers){
    List<Clothes> withOuter = new ArrayList<>();

    for(Clothes outer : outers){
      int outerScore = getWeight(outer);
      if(totalScore+outerScore <= 5 && totalScore+outerScore >= 0){
        withOuter.add(outer);
      }
    }

    return withOuter;
  }

  //날씨 가중치
  private int getWeatherValue(UUID weatherId){
    Weather weather = weatherService.getWeatherEntityByIdOrThrow(weatherId);

    double max = weather.getTemperatureMax();
    double min = weather.getTemperatureMin();

    double mid = (max+min)/2;

    for(int i : weatherCriteria.keySet()){
      if(mid < i+5 && mid >= i){
        return weatherCriteria.get(i);
      }
    }

    throw new NoSuchElementException();
  }


  //날씨 가중치 기준 생성
  @PostConstruct
  public void makeWeatherCriteria(){
    weatherCriteria.put(-15,-15);
    weatherCriteria.put(-10,-10);
    weatherCriteria.put(-5,-7);
    weatherCriteria.put(0,-5);
    weatherCriteria.put(5,2);
    weatherCriteria.put(10,5);
    weatherCriteria.put(15,7);
    weatherCriteria.put(20,10);
    weatherCriteria.put(25,12);
    weatherCriteria.put(30,15);
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
