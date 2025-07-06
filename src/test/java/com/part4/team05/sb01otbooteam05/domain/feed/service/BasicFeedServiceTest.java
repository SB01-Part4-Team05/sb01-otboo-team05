package com.part4.team05.sb01otbooteam05.domain.feed.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeValue;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.ClothesType;
import com.part4.team05.sb01otbooteam05.domain.clothes.service.ClothesService;
import com.part4.team05.sb01otbooteam05.domain.feed.entity.Feed;
import com.part4.team05.sb01otbooteam05.domain.feed.mapper.FeedMapper;
import com.part4.team05.sb01otbooteam05.domain.feed.repository.FeedRepository;
import com.part4.team05.sb01otbooteam05.domain.feedComment.entity.Comment;
import com.part4.team05.sb01otbooteam05.domain.feedComment.mapper.CommentMapper;
import com.part4.team05.sb01otbooteam05.domain.feedComment.repository.FeedCommentRepository;
import com.part4.team05.sb01otbooteam05.domain.feedLike.entity.FeedLike;
import com.part4.team05.sb01otbooteam05.domain.feedLike.repository.FeedLikeRepository;
import com.part4.team05.sb01otbooteam05.domain.ootd.entity.Ootd;
import com.part4.team05.sb01otbooteam05.domain.user.entity.GenderType;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.entity.UserRole;
import com.part4.team05.sb01otbooteam05.domain.user.service.UserService;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.PrecipitationType;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.SkyStatusType;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.WindSpeedAsWord;
import com.part4.team05.sb01otbooteam05.domain.weather.service.WeatherService;

@ExtendWith(MockitoExtension.class)
class BasicFeedServiceTest {

	@InjectMocks
	private BasicFeedService feedService;
	@Mock
	private FeedRepository feedRepository;
	@Mock
	private UserService userService;
	@Mock
	private WeatherService weatherService;
	@Mock
	private ClothesService clothesService;
	@Mock
	private FeedLikeRepository feedLikeRepository;
	@Mock
	private FeedCommentRepository feedCommentRepository;
	@Mock
	private FeedMapper feedMapper;
	@Mock
	private CommentMapper commentMapper;

	private User user;
	private Feed feed;
	private Weather weather;
	private Ootd ootd;
	private FeedLike feedLike;
	private Comment feedComment;

	// todo 하드코딩 되어있는 목 데이터 랜덤하게 생성되도록 변경
	@BeforeEach
	void setUp() {
		// 테스트 데이터 초기화
		user = userMockMaker();
		weather = weatherMockMaker();
		feed = feedMockMaker(user, weather);
		feedLike = feedLikeMockMaker(feed, user);
		feedComment = commentMockMaker(feed, user);
	}

	@Test
	void findFeeds() {

	}

	@Test
	void createFeed() {
	}

	@Test
	void deleteFeed() {
	}

	@Test
	void likeFeed() {
	}

	@Test
	void unlikeFeed() {
	}

	@Test
	void createFeedComment() {
	}

	@Test
	void updateFeed() {
	}

	@Test
	void findComments() {
	}



	private Weather weatherMockMaker() {
		return new Weather(
			60,
			127,
			LocalDateTime.of(2025, 7, 6, 9, 0),
			LocalDateTime.of(2025, 7, 6, 12, 0),
			SkyStatusType.CLOUDY,
			PrecipitationType.RAIN,
			5.2,
			80.0,
			65.0,
			-3.0,
			28.4,
			1.5,
			24.0,
			30.0,
			3.6,
			WindSpeedAsWord.WEAK
		);
	}

	private User userMockMaker() {
		return new User(
			"user@email.com",
			"username",
			"password486",
			UserRole.USER,
			false,
			GenderType.FEMALE,
			LocalDate.of(2000, 7, 12),
			37.5665,
			126.6780,
			60,
			127,
			List.of("서울특별시", "중구", "을지로동"),
			3,
			null,
			true,
			LocalDateTime.of(2025, 7, 7, 23, 59)
		);
	}

	private Feed feedMockMaker(User author, Weather weather) {
		return new Feed(author, weather, "content");
	}

	private Clothes clothesMockMaker(User owner) {
		return new Clothes(
			UUID.randomUUID(),
			"메롱시티 장갑",
			"fakeUrl.com",
			ClothesType.ACC,
			new ArrayList<AttributeValue>(),
			owner.getId()
		);
	}

	private Ootd ootdMockMaker(Feed feed, Clothes clothes) {
		return new Ootd(feed, clothes);
	}

	private FeedLike feedLikeMockMaker(Feed feed, User author) {
		return new FeedLike(feed, user);
	}

	private Comment commentMockMaker(Feed feed, User author) {
		return new Comment(feed, user, "테스트 댓글");
	}
}
