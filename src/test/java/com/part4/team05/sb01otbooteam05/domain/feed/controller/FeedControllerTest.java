package com.part4.team05.sb01otbooteam05.domain.feed.controller;

import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import com.part4.team05.sb01otbooteam05.domain.clothes.repository.ClothesRepository;
import com.part4.team05.sb01otbooteam05.domain.feed.mapper.FeedMapper;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;
import com.part4.team05.sb01otbooteam05.domain.user.service.AdminInitializer;
import com.part4.team05.sb01otbooteam05.domain.user.service.KakaoApiService;
import com.part4.team05.sb01otbooteam05.domain.weather.client.WeatherApiClient;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import com.part4.team05.sb01otbooteam05.domain.weather.repository.WeatherRepository;
import com.part4.team05.sb01otbooteam05.util.EntityProvider;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.port;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "admin.email=test@admin.com",
        "admin.password=test1234",
        "admin.name=AdminTest"
})
public class FeedControllerTest {

    @LocalServerPort
    private int randomPort;
    @Autowired
    private FeedController feedController;
    @Autowired
    private FeedMapper feedMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WeatherRepository weatherRepository;
    @Autowired
    private EntityProvider entityProvider;
    @Autowired
    private ClothesRepository clothesRepository;


    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("피드 생성 성공 테스트")
    @Test
    void givenValidRequest_whenCreateFeed_thenReturnCreatedFeedWith201() {
        //given
        User requestUser = entityProvider.createTestUserWithLocationData();
        Weather weather = entityProvider.createTestWeather();
        List<Clothes> clothesList = List.of(entityProvider.createTestClothes(requestUser, null), entityProvider.createTestClothes(requestUser, null));

        Map<String, Object> requestBody = Map.of(
                "authorId", requestUser.getId().toString(),
                "weatherId", weather.getId().toString(),
                "clothesIds", clothesList.stream().map(clothes -> clothes.getId().toString()).toList(),
                "content", "테스트 피드입니다!"
        );


        //when & then
        given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer" + requestUser.getId().toString())
                .body(requestBody)
                .when().post("/api/feeds")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("createdAt", notNullValue())
                .body("author.id", equalTo(requestUser.getId().toString()))
                .body("weather.id", equalTo(weather.getId().toString()))
                .body("ootds", notNullValue())
                .body("content", equalTo("테스트 피드입니다!"))
                .body("likeCount", equalTo(0))
                .body("commentCount", equalTo(0))
                .body("likedByMe", equalTo(false));
    }

    @TestConfiguration
    static class TestConfig {
        @Mock
        private WeatherApiClient weatherApiClient;


        @Mock
        private KakaoApiService kakaoApiService;

        @Mock
        private AdminInitializer adminInitializer;
    }
}