package com.part4.team05.sb01otbooteam05.domain.feed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.auth.security.jwt.JwtTokenProvider;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDto;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FeedCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FeedUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FindFeedsRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.enums.SortType;
import com.part4.team05.sb01otbooteam05.domain.feed.mapper.FeedMapper;
import com.part4.team05.sb01otbooteam05.domain.feed.service.FeedService;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDto;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.CommentCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.FindCommentsRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.service.FeedCommentService;
import com.part4.team05.sb01otbooteam05.domain.user.service.AdminInitializer;
import com.part4.team05.sb01otbooteam05.domain.user.service.KakaoApiService;
import com.part4.team05.sb01otbooteam05.domain.user.service.UserService;
import com.part4.team05.sb01otbooteam05.domain.weather.client.WeatherApiClient;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.PrecipitationType;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.SkyStatusType;
import com.part4.team05.sb01otbooteam05.util.EntityProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {
        "admin.email=test@admin.com",
        "admin.password=test1234",
        "admin.name=AdminTest"
})
@WebMvcTest(controllers = FeedController.class)
@Import(EntityProvider.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FeedControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityProvider entityProvider;

    @MockitoBean
    private FeedService feedService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private FeedCommentService feedCommentService;

    @MockitoBean
    private FeedMapper feedMapper;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JpaMetamodelMappingContext context;

    @MockitoBean
    private WeatherApiClient weatherApiClient;

    @MockitoBean
    private KakaoApiService kakaoApiService;

    @MockitoBean
    private AdminInitializer adminInitializer;


    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("피드 조회 성공 테스트")
    void findFeed_success() throws Exception {

        UUID userId = UUID.randomUUID();
        CustomUserDetails customUserDetails = entityProvider.createCustomUserDetailsById(userId);

        // 컨트롤러에서 비 필수값 안들어올 시 기본값으로 대체
        String cursor = "";
        UUID idAfter = null;
        SortType sortType = null;
        SkyStatusType skyStatusType = null;
        PrecipitationType precipitationType = null;
        UUID autorIdEqual = null;
        String keywordLike = "";


        // SecurityContext에 CustomUserDetails 넣기
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities()
        ));
        SecurityContextHolder.setContext(context);

        FindFeedsRequest request = FindFeedsRequest.builder().build();
        FeedDtoCursorResponse mockCursorFeedsDto = FeedDtoCursorResponse.builder().build();

        given(feedService.findFeeds(customUserDetails.getUserId(), request)).willReturn(mockCursorFeedsDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/feeds")
                        .param("limit", "10")
                        .param("sortDirection", "DESCENDING")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @DisplayName("피드 댓글 조회 성공 테스트")
    void findFeedComments_success() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID feedId = UUID.randomUUID();
        CustomUserDetails customUserDetails = entityProvider.createCustomUserDetailsById(userId);

        // 컨트롤러에서 비 필수값 안들어올 시 기본값으로 대체
        LocalDateTime cursor = null;
        UUID idAfter = null;
        Integer limit = null;

        // SecurityContext에 CustomUserDetails 넣기
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities()
        ));
        SecurityContextHolder.setContext(context);

        FindCommentsRequest request = FindCommentsRequest.builder().build();
        CommentDtoCursorResponse mockCursorCommentsDto = CommentDtoCursorResponse.builder().build();

        given(feedCommentService.findComments(customUserDetails.getUserId(), request)).willReturn(mockCursorCommentsDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/feeds/{feedId}/comments", feedId)
                        .param("feedId", feedId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @DisplayName("피드 생성 성공 테스트")
    void createFeed_success() throws Exception {
        UUID authorId = UUID.randomUUID();
        UUID weatherId = UUID.randomUUID();
        UUID clothesId = UUID.randomUUID();
        CustomUserDetails customUserDetails = entityProvider.createCustomUserDetailsById(authorId);

        // SecurityContext에 CustomUserDetails 넣기
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities()
        ));
        SecurityContextHolder.setContext(context);


        FeedCreateRequest request = new FeedCreateRequest(
                authorId,
                weatherId,
                List.of(clothesId),
                "테스트 피드입니다!"
        );

        FeedDto mockFeedDto = FeedDto.builder().build();

        given(feedService.createFeed(customUserDetails.getUserId(), request)).willReturn(mockFeedDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/feeds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();
    }

    @Test
    @DisplayName("피드 좋아요 성공 테스트")
    void likeFeed_success() throws Exception {
        UUID authorId = UUID.randomUUID();
        UUID feedId = UUID.randomUUID();
        UUID weatherId = UUID.randomUUID();
        CustomUserDetails customUserDetails = entityProvider.createCustomUserDetailsById(authorId);

        // SecurityContext에 CustomUserDetails 넣기
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities()
        ));
        SecurityContextHolder.setContext(context);

        FeedDto mockFeedDto = FeedDto.builder().build();

        given(feedService.likeFeed(customUserDetails.getUserId(), feedId)).willReturn(mockFeedDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/feeds/" + feedId + "/like")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @DisplayName("피드 좋아요 취소 성공 테스트")
    void unlikeFeed_success() throws Exception {
        UUID authorId = UUID.randomUUID();
        UUID feedId = UUID.randomUUID();
        CustomUserDetails customUserDetails = entityProvider.createCustomUserDetailsById(authorId);

        // SecurityContext에 CustomUserDetails 넣기
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities()
        ));
        SecurityContextHolder.setContext(context);

        FeedDto mockFeedDto = FeedDto.builder().build();

        given(feedService.unlikeFeed(customUserDetails.getUserId(), feedId)).willReturn(mockFeedDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/feeds/" + feedId + "/like")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @DisplayName("피드 댓글 생성 성공 테스트")
    void createFeedComment_success() throws Exception {


        UUID authorId = UUID.randomUUID();
        UUID feedId = UUID.randomUUID();
        CustomUserDetails customUserDetails = entityProvider.createCustomUserDetailsById(authorId);

        // SecurityContext에 CustomUserDetails 넣기
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities()
        ));
        SecurityContextHolder.setContext(context);


        CommentCreateRequest request =
                CommentCreateRequest.builder().authorId(authorId).feedId(feedId).content("테스트 피드입니다!").build();

        CommentDto mockCommentDto = CommentDto.builder().build();

        given(feedCommentService.createFeedComment(customUserDetails.getUserId(), feedId, request)).willReturn(mockCommentDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/feeds/{feedId}/comments", feedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();
    }

    @Test
    @DisplayName("피드 삭제 성공 테스트")
    void deleteFeed_success() throws Exception {

        UUID authorId = UUID.randomUUID();
        UUID feedId = UUID.randomUUID();
        CustomUserDetails customUserDetails = entityProvider.createCustomUserDetailsById(authorId);

        // SecurityContext에 CustomUserDetails 넣기
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities()
        ));
        SecurityContextHolder.setContext(context);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/feeds/" + feedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    @DisplayName("피드 수정 성공 테스트")
    void updateFeed_success() throws Exception {
        UUID authorId = UUID.randomUUID();
        UUID feedId = UUID.randomUUID();
        CustomUserDetails customUserDetails = entityProvider.createCustomUserDetailsById(authorId);

        // SecurityContext에 CustomUserDetails 넣기
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities()
        ));
        SecurityContextHolder.setContext(context);


        FeedUpdateRequest request = FeedUpdateRequest.builder()
                .content("변경될 테스트 피드입니다!!").build();

        FeedDto mockFeedDto = FeedDto.builder().build();

        given(feedService.updateFeed(customUserDetails.getUserId(), feedId, request)).willReturn(mockFeedDto);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/feeds/{feedId}", feedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
    }

    /*@LocalServerPort
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
    }*/
}