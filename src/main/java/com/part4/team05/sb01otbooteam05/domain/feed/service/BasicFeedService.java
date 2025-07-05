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
import com.part4.team05.sb01otbooteam05.domain.feed.exception.FeedNotFoundException;
import com.part4.team05.sb01otbooteam05.domain.feed.mapper.FeedMapper;
import com.part4.team05.sb01otbooteam05.domain.feed.repository.FeedRepository;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDto;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentsPageResponse;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.CommentCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.FindCommentsRequest;
import com.part4.team05.sb01otbooteam05.domain.feedComment.entity.Comment;
import com.part4.team05.sb01otbooteam05.domain.feedComment.mapper.CommentMapper;
import com.part4.team05.sb01otbooteam05.domain.feedComment.repository.FeedCommentRepository;
import com.part4.team05.sb01otbooteam05.domain.feedLike.entity.FeedLike;
import com.part4.team05.sb01otbooteam05.domain.feedLike.repository.FeedLikeRepository;
import com.part4.team05.sb01otbooteam05.domain.ootd.entity.Ootd;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.service.UserService;
import com.part4.team05.sb01otbooteam05.domain.user.service.UserServiceImpl;
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
	private final FeedLikeRepository feedLikeRepository;
	private final FeedCommentRepository feedCommentRepository;
	private final FeedMapper feedMapper;
	private final CommentMapper commentMapper;
	private final UserServiceImpl userServiceImpl;


	@Override
	@Transactional(readOnly = true)
	public FeedsPageResponse findFeeds(UUID userId, FindFeedsRequest request) {
		return null;
	}

	// 피드 생성
	@Override
	public FeedDto createFeed(UUID userId, FeedCreateRequest request) {

		checkUserIdEquality(userId, request.authorId());

		// 1. 유저, 날씨 객체 조회
		User user = userService.getUserEntityByIdOrThrow(userId);
		Weather weather = weatherService.getWeatherEntityByIdOrThrow(request.weatherId());
		// 2. 피드 객체 생성
		Feed newFeed = new Feed(user, weather, request.content());

		/* 3. 피드에 들어갈 옷 종류 조회
			일부가 조회 실패하더라도 예외 던지지 않고, 조회 성공한 옷들만 추가함. */
		Stream<Optional<Clothes>> foundClothesStream = request.clothesIds()
			.stream()
			.map(clothesId -> clothesService.getClothesEntityById(clothesId));

		// 4. 옷 단건을 참조하는 Ootd 객체 생성과 동시에 피드 ootds 필드에 추가 (Feed <- Ootd -> Clothes)
		foundClothesStream.forEach(clothes -> new Ootd(newFeed));

		// 5. 피드 내 ootds 리스트에 ootd 객체가 하나라도 있으면 Feed 저장. 없다면 Feed 생성 실패.
		// todo 예외 종류가 적절한지 고민 필요
		if (newFeed.getOotds().isEmpty()) {
			throw new IllegalArgumentException();
		}

		log.info("피드 생성 성공: feedId={}", newFeed.getId());
		return feedMapper.toDto(newFeed, 0L, 0, true);
	}

	// 피드 삭제
	@Override
	public void deleteFeed(UUID userId, UUID feedId) {

		log.info("피드 삭제 시작: feedId={}", feedId);

		// 1. 피드, 유저 조회
		Feed feed = feedRepository.findById(feedId).orElseThrow(() -> FeedNotFoundException.withId(feedId));
		User author = userService.getUserEntityByIdOrThrow(userId);

		// 2. 피드 작성자와 요청한 유저가 동일한지 검증
		checkUserIdEquality(feed.getAuthor().getId(), author.getId());

		// todo 피드를 삭제한다고 연관객체들을 다 날리는게 맞을까 ? 고민하고 수정하기
		// 3. 피드 연관 객체(댓글, 좋아요, ootd), 피드 삭제
		feedCommentRepository.deleteAllByFeed(feed);
		feedLikeRepository.deleteAllByFeed(feed);
		feedRepository.delete(feed);

		log.info("피드 삭제 성공");
	}

	// 피드 좋아요
	@Override
	public FeedDto likeFeed(UUID userId, UUID feedId) {

		// 1. 피드, 유저 조회
		User author = userService.getUserEntityByIdOrThrow(userId);
		Feed feed = feedRepository.findById(feedId).orElseThrow(() -> FeedNotFoundException.withId(feedId));

		// 2. 현재 좋아요 수 조회
		Long currentLikeCount = feedLikeRepository.countByFeed(feed);

		// 3. 해당 유저가 피드에 이미 좋아요 했는지 여부를 확인
		Optional<FeedLike> foundFeedLike = feedLikeRepository.findByFeedAndAuthor(feed, author);
		boolean isLikeExistent = foundFeedLike.isPresent();

		// 4. 좋아요 객체 존재하지 않을 시 FeedLike 객체 생성하고 저장
		if (!isLikeExistent) {
			feedLikeRepository.save(new FeedLike(feed, author));
			currentLikeCount++; // 좋아요 수 1 증가
		}

		// 5. 피드 Dto 반환
		Integer commentCount = feedCommentRepository.countByFeed(feed);
		Boolean likedByMe = true;

		log.info("피드 좋아요 성공: feedId={}", feed.getId());
		return feedMapper.toDto(feed, currentLikeCount, commentCount, likedByMe);
	}

	@Override
	public FeedDto unlikeFeed(UUID userId, UUID feedId) {

		// 1. 피드, 유저 조회
		User author = userService.getUserEntityByIdOrThrow(userId);
		Feed feed = feedRepository.findById(feedId).orElseThrow(() -> FeedNotFoundException.withId(feedId));

		// 2. 현재 좋아요 수 조회
		Long currentLikeCount = feedLikeRepository.countByFeed(feed);

		// 3. FeedLike 객체가 존재하여, 삭제되었다면 좋아요 수 1 감소
		if (feedLikeRepository.deleteByFeedAndAuthor(feed, author) > 0) {
			currentLikeCount --;
		}
		Integer commentCount = feedCommentRepository.countByFeed(feed);
		Boolean likedByMe = false;

		// 4. 피드 Dto 반환
		log.info("피드 좋아요 취소 성공: feedId={}", feed.getId());
		return feedMapper.toDto(feed, currentLikeCount, commentCount, likedByMe);
	}

	@Override
	public CommentDto createFeedComment(UUID userId, UUID feedId, CommentCreateRequest request) {

		checkUserIdEquality(userId, request.authorId());

		// 1. 피드, 유저 조회
		Feed feed = feedRepository.findById(feedId).orElseThrow(() -> FeedNotFoundException.withId(feedId));
		User author = userService.getUserEntityByIdOrThrow(userId);

		// 2. 댓글 생성
		Comment newComment = new Comment(feed, author, request.content());
		feedCommentRepository.save(newComment);

		// 3. 댓글 Dto 반환
		log.info("댓글 생성 성공: commentId={}", newComment.getId());
		return commentMapper.toDto(newComment);
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

	// todo 유저 검증 실패 관련 예외로 변경하기
	// 요청 Id와 파라미터 Id가 같은지 검증
	public void checkUserIdEquality(UUID firstId, UUID secondId) {
		if (!firstId.equals(secondId)){ throw new IllegalArgumentException(); }
	}
}
