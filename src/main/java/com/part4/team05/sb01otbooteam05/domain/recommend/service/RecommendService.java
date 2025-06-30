package com.part4.team05.sb01otbooteam05.domain.recommend.service;

import com.part4.team05.sb01otbooteam05.domain.attribute.entity.StyleType;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.ThicknessType;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.ClothesType;
import com.part4.team05.sb01otbooteam05.domain.clothes.service.ClothesService;
import jakarta.annotation.PostConstruct;
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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendService {
  private final ClothesService clothesService;

  private final Random random = new Random();
  private final Map<ThicknessType,Integer> criteria = new HashMap<>(); // 옷 두께 가중치

  public List<List<Clothes>> getRecommend(UUID ownerId){
    Map<StyleType, Map<ClothesType,List<Clothes>>> map = getMap(ownerId);
    List<List<Clothes>> result = new ArrayList<>();

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

      scoreMap = getScore(tops,bottoms);
      List<List<Clothes>> settings = getSetting(scoreMap,bottoms,outers);

      settings.addAll(getDressScore(innerMap.getOrDefault(ClothesType.DRESS,Collections.emptyList()),
          outers));

      int time = 0;

      while(time < 3){
        int indexList = random.nextInt(settings.size());

        int indAcc = random.nextInt(acc.size());
        List<Clothes> setting = new ArrayList<>(settings.get(indexList));
        setting.add(acc.get(indAcc));

        result.add(setting);
        time++;
      }

      result.addAll(settings);
    }

    return result;
  }

  private List<List<Clothes>> getDressScore(List<Clothes> dresses,List<Clothes> outers) {
    List<List<Clothes>> list = new ArrayList<>();

    for(Clothes dress : dresses){
      int totalScore = -Math.abs(getWeight(dress)/* - 날씨 가중치*/);
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

  @PostConstruct
  public void makeCriteria(){
    criteria.put(ThicknessType.THICK, 5);
    criteria.put(ThicknessType.SLIMTHICK, 2);
    criteria.put(ThicknessType.SLIMTHIN,-2);
    criteria.put(ThicknessType.THIN,-5);
  }

  private Map<Clothes,List<Integer>> getScore(List<Clothes> tops, List<Clothes> bottoms){
    Map<Clothes,List<Integer>> map = new HashMap<>();

    for(Clothes top : tops){
      int topScore = getWeight(top);
      List<Integer> scores = new ArrayList<>();
      for(Clothes bottom : bottoms){
        int bottomScore = getWeight(bottom);
        scores.add(-Math.abs(topScore-bottomScore /* -날씨 가중치 */) - (topScore-bottomScore));
      }
      map.put(top,scores);
    }

    return map;
  }

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


  /*
  추천 기준
    최저 - 최고 중간 값을 기준
    속성 값을 통해 추천 -> 두께에 가중치를 둬서 특정 가중치가 나오도록 조합
    의상을 두께 인덱스로 정렬할 필요성이 있는가?
    유저 ID를 통해서 옷 리스트를 가지고 옴 -> 타입별로 나눔 -> 가중치 별로 뽑음.. 효율적인지?
    => 상+하 의 가중치만 보기 ++아우터는 선택사항.. + 악세사리 없을 때 / 있을 때
    => 악세사리는 같은 스타일 타입에 존재하는 악세사리 3개 랜덤 선택 후 추천 세트에 포함시키기
     날씨에서 가져올 것
     온도 최고 / 최저
     온도 미디언 값 -> 가중치 확인
     온도 가중치는 5도 단위로 끊고 -15 ~ 35 까지
     -15 / -10 / -7 / -5 / 2 / 5 / 7 / 10 / 12 / 15
     옷 가중치 + 온도 가중치가 0 ~ 5 사이가 되도록 하는 것이 추천의 목표

     score = -Math.abs(상의 - 하의) - 상하의 가중치 차이값 <= 허용 오차
   */
}
