package com.part4.team05.sb01otbooteam05.domain.recommend.service;

import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeDefinition;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeValue;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.StyleType;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.ThicknessType;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.ClothesType;
import com.part4.team05.sb01otbooteam05.domain.clothes.service.ClothesService;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendService {
  private final ClothesService clothesService;

  private final Map<ThicknessType,Integer> criteria = new HashMap<>(); // 옷 두께 가중치

  public List<Clothes> getRecommend(UUID ownerId){
    Map<StyleType, Map<ClothesType,List<Clothes>>> map = getMap(ownerId);
    //날씨를 불러오는 로직 // 날씨 가중치도 있어야 할까?
    //스타일별, 의상 타입별 분류 완료

    for(StyleType type : map.keySet()){

    }


    return Collections.emptyList();
  }

  private Map<StyleType, Map<ClothesType,List<Clothes>>> getMap(UUID ownerId){
    List<Clothes> allClothes = clothesService.findAllByOwnerId(ownerId);


    return allClothes.stream()
        .collect(Collectors.groupingBy(
            clothes -> clothes.getAttributeValues().stream()
                .filter(av -> av.getDefinition().getName().equals("style"))
                .map(av -> StyleType.valueOf(av.getValue()))
                .findFirst()
                .orElseThrow(NoSuchFieldError::new),
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
  /*
  추천 기준
    최저 - 최고 중간 값을 기준
    속성 값을 통해 추천 -> 두께에 가중치를 둬서 특정 가중치가 나오도록 조합
    의상을 두께 인덱스로 정렬할 필요성이 있는가?
    유저 ID를 통해서 옷 리스트를 가지고 옴 -> 타입별로 나눔 -> 가중치 별로 뽑음.. 효율적인지?
    => 상+하 의 가중치만 보기 ++아우터는 선택사항.. + 악세사리 없을 때 / 있을 때

     날씨에서 가져올 것
     온도 최고 / 최저
     온도 미디언 값 -> 가중치 확인
     온도 가중치는 5도 단위로 끊고 -15 ~ 35 까지
     -15 / -10 / -7 / -5 / 2 / 5 / 7 / 10 / 12 / 15
     옷 가중치 + 온도 가중치가 0 ~ 5 사이가 되도록 하는 것이 추천의 목표

     score = -Math.abs(상의 - 하의) - 상하의 가중치 차이값 <= 허용 오차
   */
}
