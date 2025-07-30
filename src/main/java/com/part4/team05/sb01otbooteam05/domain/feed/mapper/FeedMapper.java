package com.part4.team05.sb01otbooteam05.domain.feed.mapper;

import static com.part4.team05.sb01otbooteam05.domain.weather.mapper.WeatherMapper.*;

import java.util.List;

import org.springframework.stereotype.Component;

import com.part4.team05.sb01otbooteam05.domain.feed.dto.AuthorDto;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDto;
import com.part4.team05.sb01otbooteam05.domain.feed.entity.Feed;
import com.part4.team05.sb01otbooteam05.domain.feedComment.repository.FeedCommentRepository;
import com.part4.team05.sb01otbooteam05.domain.feedLike.repository.FeedLikeRepository;
import com.part4.team05.sb01otbooteam05.domain.ootd.dto.OotdDto;
import com.part4.team05.sb01otbooteam05.domain.ootd.entity.Ootd;
import com.part4.team05.sb01otbooteam05.domain.ootd.mapper.OotdMapper;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class FeedMapper {

	private final FeedLikeRepository feedLikeRepository;
	private final FeedCommentRepository feedCommentRepository;
	private final OotdMapper ootdMapper;

	// 각 피드마다 조회한 user의 좋아요 여부를 확인하기 위해 유저 또한 dto 생성에 파라미터로 주입된다.
	public List<FeedDto> toFeedDtoList(List<Feed> feeds, User user){
		return feeds.stream().map(
				feed -> toFeedDto(feed, countFeedLikeByFeed(feed), countCommentByFeed(feed), isLikedByMe(feed, user)
				)).toList();
	}

	public AuthorDto toAuthorDto(User user){
		return new AuthorDto(user.getId(), user.getName(), user.getProfileImageUrl());
	}

	public OotdDto toOotdDto(Ootd ootd){
		return ootdMapper.toOotdDto(ootd);
	}

	public List<OotdDto> toOotdDtoList(List<Ootd> ootds) {
		return ootds.stream().map(ootd->toOotdDto(ootd)).toList();
	}

	public FeedDto toFeedDto(Feed feed, Long likeCount, Integer commentCount, Boolean likedByMe) {
		return new FeedDto(
				feed.getId(),
				feed.getCreatedAt(),
				feed.getUpdatedAt(),
				toAuthorDto(feed.getAuthor()),
				toSummaryDto(feed.getWeather()),
				toOotdDtoList(feed.getOotds()),
				feed.getContent(),
				likeCount,
				commentCount,
				likedByMe
		);
	}

	public FeedDto toFeedDto(Feed feed, User user) {
		return toFeedDto(
		feed,
		countFeedLikeByFeed(feed),
		countCommentByFeed(feed),
		isLikedByMe(feed, user)
		);
	}

	private Long countFeedLikeByFeed(Feed feed) {
		return feedLikeRepository.countByFeed(feed);
	}

	private Integer countCommentByFeed(Feed feed) {
		return feedCommentRepository.countByFeed(feed);
	}

	private boolean isLikedByMe(Feed feed, User user){
		return feedLikeRepository.findByFeedAndAuthor(feed, user).isPresent();
	}

	// 피드매퍼 로직을 전반적으로 수정했는데, 민주님이 작성해주신 코드들은 이후에 사용하실까봐 주석처리해두었어요 !
	/*@Mapping(source = "weather.windSpeed", target = "weather.windSpeed", qualifiedByName = "windSpeedDtoToDouble")
	Feed toEntity(FeedDto feedDto);*/

	/*@Named("windSpeedDtoToDouble")
	default Double map(WindSpeedDto dto) {
		return dto != null ? dto.speed() : null;
	}*/
}
