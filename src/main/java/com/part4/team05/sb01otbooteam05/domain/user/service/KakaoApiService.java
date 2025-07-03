package com.part4.team05.sb01otbooteam05.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoApiService {

  private final RestTemplate restTemplate;

  @Value("${kakao.rest-api-key}")
  private String kakaoApiKey;

  private static final String KAKAO_API_URL = "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json";

  public List<String> getLocationNames(double latitude, double longitude) {
    try {
      String url = UriComponentsBuilder.fromHttpUrl(KAKAO_API_URL)
          .queryParam("x", longitude)
          .queryParam("y", latitude)
          .toUriString();

      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization", "KakaoAK " + kakaoApiKey);

      HttpEntity<String> entity = new HttpEntity<>(headers);

      ResponseEntity<Map> response = restTemplate.exchange(
          url,
          HttpMethod.GET,
          entity,
          Map.class
      );

      List<String> locationNames = new ArrayList<>();

      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        List<Map<String, Object>> documents = (List<Map<String, Object>>) response.getBody().get("documents");

        if (documents != null && !documents.isEmpty()) {
          for (Map<String, Object> doc : documents) {
            String regionType = (String) doc.get("region_type");
            if ("H".equals(regionType)) { // 행정동(H)만 -> 프로토타입상 행정동만 출력되고 있어 이렇게 결정
              locationNames.add((String) doc.get("address_name"));
            }
          }
        }
      }

      return locationNames;

    } catch (Exception e) {
      log.error("카카오 API 호출 실패: latitude={}, longitude={}", latitude, longitude, e);
      // 실패 시 빈 리스트 반환
      return new ArrayList<>();
    }
  }
}
