package com.part4.team05.sb01otbooteam05.domain.feed.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.hibernate.query.SortDirection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.PropertyResolver;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team05.sb01otbooteam05.Sb01OtbooTeam05Application;
import com.part4.team05.sb01otbooteam05.domain.base.BaseEntity;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDto;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FeedCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FeedUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FindFeedsRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.entity.Feed;
import com.part4.team05.sb01otbooteam05.domain.feed.enums.SortType;
import com.part4.team05.sb01otbooteam05.domain.feed.mapper.FeedMapper;
import com.part4.team05.sb01otbooteam05.domain.feed.repository.FeedRepository;
import com.part4.team05.sb01otbooteam05.domain.feedLike.entity.FeedLike;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import com.part4.team05.sb01otbooteam05.util.EntityProvider;

@TestPropertySource(properties = {"admin.email=test@admin.com", "admin.password=test1234", "admin.name=AdminTest"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Sb01OtbooTeam05Application.class)
@Import(EntityProvider.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
	// @Rollback(value = false) <- yml파일 ddl-auto: create 옵션 설정 후, 이거 false 옵션 주면 테스트 자동롤백 해제되어 db 확인 가능
class BasicFeedServiceTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private EntityProvider entityProvider;

	@Autowired
	private BasicFeedService feedService;

	private User user;
	private Weather weather;
	private Clothes clothes1;
	private Clothes clothes2;
	private Feed feed;
	private FeedLike feedLike;

	@Autowired
	private FeedRepository feedRepository;
	@Autowired
	private FeedMapper feedMapper;
	@Autowired
	private PropertyResolver propertyResolver;

	@BeforeEach
	void before() {
		user = entityProvider.createTestUsers(1).get(0);
		weather = entityProvider.createTestWeather();
		clothes1 = entityProvider.createTestClothes(user, null);
		clothes2 = entityProvider.createTestClothes(user, null);
		feed = entityProvider.createTestFeed(user, List.of(clothes1, clothes2), weather);
		feedLike = entityProvider.createTestFeedLike(user, feed);
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("피드 조회 성공 테스트_디폴트 옵션 및 최신순")
	void findFeedsWithDefaultOptionsAndCreatedAt_success() throws Exception {
		List<User> authors = entityProvider.createTestUsers(5);
		List<Feed> feeds = entityProvider.createTestFeeds(30, authors, List.of(clothes1, clothes2), weather);
		feeds.add(feed);
		List<Feed> descCreatedAtSortedfeeds = feeds.stream()
			.sorted(Comparator.comparing(Feed::getCreatedAt).reversed())
			.toList();

		FindFeedsRequest request = FindFeedsRequest.builder()
			.limit(10)
			.sortDirection(SortDirection.DESCENDING)
			.sortBy(SortType.createdAt)
			.authorIdEqual(null)
			.keywordLike("")
			.idAfter(null)
			.skyStatusEqual(null)
			.precipitationTypeEqual(null)
			.cursor("")
			.build();

		FeedDtoCursorResponse feedDtoCursorResponse = feedService.findFeeds(user.getId(), request);

		Assertions.assertThat(feedDtoCursorResponse.nextCursor()).isNotNull();
		Assertions.assertThat(feedDtoCursorResponse.nextIdAfter()).isNotNull();
		Assertions.assertThat(descCreatedAtSortedfeeds.stream().map(BaseEntity::getCreatedAt).toList())
			.isEqualTo(descCreatedAtSortedfeeds.stream().map(BaseEntity::getCreatedAt).toList());
	}

	@Test
	@DisplayName("피드 생성 성공 테스트")
	void createFeed_success() throws Exception {
		List<UUID> clothesIds = List.of(clothes1.getId(), clothes2.getId());
		FeedCreateRequest request = FeedCreateRequest.builder()
			.authorId(user.getId())
			.clothesIds(clothesIds)
			.weatherId(weather.getId())
			.content("피드 생성 테스트!!!")
			.build();
		Assertions.assertThat(feedService.createFeed(user.getId(), request)).isNotNull();
	}

	@Test
	@DisplayName("피드 삭제 성공 테스트")
	void deleteFeed_success() throws Exception {
		Assertions.assertThat(feedRepository.findById(feed.getId()).get()).isEqualTo(feed);
		feedService.deleteFeed(user.getId(), feed.getId());
		Assertions.assertThat(feedRepository.findById(feed.getId()).isEmpty());
	}

	@Test
	@DisplayName("피드 좋아요 성공 테스트")
	void likeFeed_success() throws Exception {
		User user = entityProvider.createTestUsers(1).get(0);
		Assertions.assertThat(feedService.likeFeed(user.getId(), feed.getId()).id())
			.isEqualTo(feedMapper.toFeedDto(feed, user).id());
	}

	@Test
	@DisplayName("피드 좋아요 취소 성공 테스트")
	void unlikeFeed_success() throws Exception {
		Long oldLikeCount = feed.getLikeCount();
		FeedDto responseFeed = feedService.unlikeFeed(user.getId(), feed.getId());
		Assertions.assertThat(responseFeed.id()).isEqualTo(feedMapper.toFeedDto(feed, user).id());
		Assertions.assertThat(responseFeed.commentCount()).isEqualTo(oldLikeCount - 1);
	}

	@Test
	@DisplayName("피드 업데이트 성공 테스트")
	void updateFeed_success() throws Exception {
		FeedUpdateRequest request = FeedUpdateRequest.builder().content("업데이트된 content").build();
		FeedDto responseFeed = feedService.updateFeed(user.getId(), feed.getId(), request);
		Assertions.assertThat(responseFeed.content()).isEqualTo("업데이트된 content");
	}
}

