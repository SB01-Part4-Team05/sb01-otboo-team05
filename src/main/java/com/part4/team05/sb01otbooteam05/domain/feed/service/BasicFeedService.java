package com.part4.team05.sb01otbooteam05.domain.feed.service;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import com.part4.team05.sb01otbooteam05.domain.clothes.service.ClothesService;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDto;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedsPageResponse;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FeedCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FeedUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FindFeedsRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.entity.Feed;
import com.part4.team05.sb01otbooteam05.domain.feed.mapper.FeedMapper;
import com.part4.team05.sb01otbooteam05.domain.feed.repository.FeedRepository;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDto;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentsPageResponse;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.CommentCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.FindCommentsRequest;
import com.part4.team05.sb01otbooteam05.domain.ootd.entity.Ootd;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.service.UserService;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import com.part4.team05.sb01otbooteam05.domain.weather.service.WeatherService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BasicFeedService implements FeedService {

	private final FeedRepository feedRepository;
	private final UserService userService;
	private final WeatherService weatherService;
	private final ClothesService clothesService;
	private final FeedMapper mapper;

	@Override
	@Transactional(readOnly = true)
	public FeedsPageResponse findFeeds(UUID userId, FindFeedsRequest request) {
		return null;
	}

	// 피드 생성
	@Override
	public FeedDto createFeed(UUID userId, FeedCreateRequest request) {

		// 1. 유저, 날씨 객체 조회
		User user = userService.getUserEntityByIdOrThrow(userId);
		Weather weather = weatherService.getWeatherEntityByIdOrThrow(request.weatherId());
		// 2. 피드 객체 생성
		Feed newFeed = new Feed(user, weather, request.content());

		// 3. 피드에 들어갈 옷 종류 조회
		Stream<Optional<Clothes>> foundClothesStream = request.clothesIds()
			.stream()
			.map(clothesId -> clothesService.getClothesEntityById(clothesId));

		// 4. 옷 단건을 참조하는 Ootd 객체 생성과 동시에 피드 ootds 필드에 추가 (Feed <- Ootd -> Clothes)
		foundClothesStream.forEach(clothes -> new Ootd(newFeed));

		// 5. 저장
		Feed savedFeed = feedRepository.save(newFeed);

		log.info("피드 생성 완료: feedId={}", savedFeed.getId());
		return mapper.toDto(savedFeed);
	}

	@Override
	public FeedDto likeFeed(UUID userId, UUID feedId) {
		return null;
	}

	@Override
	public FeedDto unlikeFeed(UUID userId, UUID feedId) {
		return null;
	}

	@Override
	public CommentDto createFeedComment(UUID userId, UUID feedId, CommentCreateRequest request) {
		return null;
	}

	@Override
	public FeedDto deleteFeed(UUID userId, UUID feedId) {
		return null;
	}

	@Override
	public FeedDto updateFeed(UUID userId, UUID feedId, FeedUpdateRequest request) {
		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public CommentsPageResponse findComments(UUID userId, FindCommentsRequest request) {
		return null;
	}
}
