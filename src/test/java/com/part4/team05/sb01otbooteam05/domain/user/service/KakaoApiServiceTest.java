package com.part4.team05.sb01otbooteam05.domain.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("카카오 API 서비스 핵심 테스트")
class KakaoApiServiceTest {

  @Mock private RestTemplate restTemplate;
  @InjectMocks private KakaoApiService kakaoApiService;

  private final String TEST_API_KEY = "test-api-key";
  private final double TEST_LATITUDE = 37.5665;
  private final double TEST_LONGITUDE = 126.9780;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(kakaoApiService, "kakaoApiKey", TEST_API_KEY);
  }

  @Test
  @DisplayName("위치명 조회 성공")
  void getLocationNames_Success() {
    Map<String, Object> responseBody = createSuccessResponseBody();
    ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

    when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
        .thenReturn(responseEntity);

    List<String> result = kakaoApiService.getLocationNames(TEST_LATITUDE, TEST_LONGITUDE);

    assertThat(result).isNotEmpty();
    assertThat(result).contains("서울특별시 중구 명동2가");
    verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));
  }

  @Test
  @DisplayName("위치명 조회 실패 - API 호출 예외")
  void getLocationNames_ApiException() {
    when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
        .thenThrow(new RuntimeException("API 호출 실패"));

    List<String> result = kakaoApiService.getLocationNames(TEST_LATITUDE, TEST_LONGITUDE);

    assertThat(result).isEmpty();
    verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));
  }

  @Test
  @DisplayName("위치명 조회 실패 - 빈 응답")
  void getLocationNames_EmptyResponse() {
    ResponseEntity<Map> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

    when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
        .thenReturn(responseEntity);

    List<String> result = kakaoApiService.getLocationNames(TEST_LATITUDE, TEST_LONGITUDE);

    assertThat(result).isEmpty();
    verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));
  }

  private Map<String, Object> createSuccessResponseBody() {
    Map<String, Object> document = new HashMap<>();
    document.put("region_type", "H");
    document.put("address_name", "서울특별시 중구 명동2가");

    List<Map<String, Object>> documents = Arrays.asList(document);

    Map<String, Object> responseBody = new HashMap<>();
    responseBody.put("documents", documents);

    return responseBody;
  }
}
