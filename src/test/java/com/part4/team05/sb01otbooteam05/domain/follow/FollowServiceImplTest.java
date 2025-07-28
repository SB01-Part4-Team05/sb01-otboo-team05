package com.part4.team05.sb01otbooteam05.domain.follow;

import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowDto;
import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowListResponse;
import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowSummaryDto;
import com.part4.team05.sb01otbooteam05.domain.follow.entity.Follow;
import com.part4.team05.sb01otbooteam05.domain.follow.exception.FollowException;
import com.part4.team05.sb01otbooteam05.domain.follow.mapper.FollowMapper;
import com.part4.team05.sb01otbooteam05.domain.follow.repository.FollowRepository;
import com.part4.team05.sb01otbooteam05.domain.follow.service.impl.FollowServiceImpl;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;
import com.part4.team05.sb01otbooteam05.domain.user.dto.UserSummary;
import com.part4.team05.sb01otbooteam05.domain.notification.service.NotificationService;
import com.part4.team05.sb01otbooteam05.exception.ErrorCode;
import com.part4.team05.sb01otbooteam05.exception.OtbooException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
// 불필요한 스터빙 경고를 띄우지 않도록 lenient 모드로 설정
@MockitoSettings(strictness = Strictness.LENIENT)
class FollowServiceImplTest {

    @Mock             FollowRepository    followRepository;
    @Mock             UserRepository      userRepository;
    @Mock             FollowMapper        followMapper;
    @Mock             NotificationService notificationService;
    @InjectMocks
    FollowServiceImpl service;

    UUID followerId = UUID.randomUUID();
    UUID followeeId = UUID.randomUUID();

    @Nested
    @DisplayName("createFollow")
    class CreateFollow {

        @Test @DisplayName("성공: 알림 전송까지 정상")
        void success() {
            //--- given ---
            FollowCreateRequest req = new FollowCreateRequest(followeeId, followerId);
            given(userRepository.existsById(followerId)).willReturn(true);
            given(userRepository.existsById(followeeId)).willReturn(true);
            given(followRepository.existsByFollowerAndFollowee(followerId, followeeId))
                    .willReturn(false);

            // spy 엔티티 + id stub
            Follow entity = spy(new Follow(followerId, followeeId));
            UUID savedId = UUID.randomUUID();
            doReturn(savedId).when(entity).getId();
            given(followMapper.toEntity(req)).willReturn(entity);
            given(followRepository.save(entity)).willReturn(entity);

            // mock User 두 개, 각각 다른 getId() 리턴
            User uFollower = mock(User.class, "uFollower");
            given(uFollower.getId()).willReturn(followerId);
            given(uFollower.getName()).willReturn("Bob");
            given(uFollower.getProfileImageUrl()).willReturn(null);

            User uFollowee = mock(User.class, "uFollowee");
            given(uFollowee.getId()).willReturn(followeeId);
            given(uFollowee.getName()).willReturn("Alice");
            given(uFollowee.getProfileImageUrl()).willReturn(null);

            given(userRepository.findAllById(List.of(followerId, followeeId)))
                    .willReturn(List.of(uFollower, uFollowee));

            FollowDto expectedDto = new FollowDto(
                    savedId,
                    new UserSummary(followeeId, "Alice", null),
                    new UserSummary(followerId,  "Bob",   null)
            );
            given(followMapper.toDto(entity, Map.of(
                    followerId, uFollower,
                    followeeId, uFollowee
            ))).willReturn(expectedDto);

            //--- when ---
            FollowDto result = service.createFollow(req);

            //--- then ---
            assertThat(result).isEqualTo(expectedDto);
            then(notificationService).should()
                    .createAndSendNotification(
                            eq(followeeId),
                            eq("새로운 팔로워"),
                            contains("Bob"),
                            any()
                    );
        }

        @Test @DisplayName("실패: 팔로워가 없음")
        void followerNotFound() {
            given(userRepository.existsById(followerId)).willReturn(false);
            assertThatThrownBy(() -> service.createFollow(new FollowCreateRequest(followeeId, followerId)))
                    .isInstanceOf(FollowException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test @DisplayName("실패: 자기 자신 팔로우 금지")
        void selfFollowNotAllowed() {
            given(userRepository.existsById(followerId)).willReturn(true);
            assertThatThrownBy(() -> service.createFollow(new FollowCreateRequest(followerId, followerId)))
                    .isInstanceOf(FollowException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.FOLLOW_SELF_NOT_ALLOWED);
        }

        @Test @DisplayName("실패: 이미 팔로우됨")
        void alreadyFollowed() {
            given(userRepository.existsById(followerId)).willReturn(true);
            given(userRepository.existsById(followeeId)).willReturn(true);
            given(followRepository.existsByFollowerAndFollowee(followerId, followeeId))
                    .willReturn(true);

            assertThatThrownBy(() -> service.createFollow(new FollowCreateRequest(followeeId, followerId)))
                    .isInstanceOf(FollowException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.ALREADY_FOLLOWED);
        }
    }

    @Nested
    @DisplayName("getFollowSummary")
    class Summary {

        @Test @DisplayName("성공: 요약 조회")
        void success() {
            given(followRepository.countByFollowee(followeeId)).willReturn(2L);
            given(followRepository.countByFollower(followeeId)).willReturn(3L);
            given(followRepository.existsByFollowerAndFollowee(followerId, followeeId)).willReturn(true);
            given(followRepository.existsByFollowerAndFollowee(followeeId, followerId)).willReturn(false);

            FollowSummaryDto dto = service.getFollowSummary(followeeId, followerId);

            assertThat(dto.followeeId()).isEqualTo(followeeId);
            assertThat(dto.followerCount()).isEqualTo(2L);
            assertThat(dto.followingCount()).isEqualTo(3L);
            assertThat(dto.followedByMe()).isTrue();
            assertThat(dto.followedByMeId()).isEqualTo(followerId);
            assertThat(dto.followingMe()).isFalse();
        }
    }

    @Nested
    @DisplayName("getFollowings / getFollowers")
    class ListPaging {

        @Test @DisplayName("성공: 팔로잉 페이징")
        void getFollowings_success() {
            given(userRepository.existsById(followerId)).willReturn(true);

            Follow f = spy(new Follow(followerId, followeeId));
            UUID mid = UUID.randomUUID();
            doReturn(mid).when(f).getId();

            // idAfter, nameLike 모두 null 로 호출되므로 isNull() 로 매칭
            given(followRepository.findFollowings(eq(followerId), isNull(), isNull(), any(PageRequest.class)))
                    .willReturn(List.of(f));

            // 반환된 Follow 에 포함된 followerId / followeeId 를 리턴하도록 stub
            User u1 = mock(User.class, "fUser"); given(u1.getId()).willReturn(followerId);
            User u2 = mock(User.class, "eUser"); given(u2.getId()).willReturn(followeeId);
            given(userRepository.findAllById(List.of(followerId, followeeId)))
                    .willReturn(List.of(u1, u2));

            FollowListResponse resp = service.getFollowings(followerId, /*cursor*/null, /*idAfter*/null, 5, /*nameLike*/null);

            assertThat(resp.data()).hasSize(1);
            assertThat(resp.hasNext()).isFalse();
            assertThat(resp.totalCount()).isEqualTo( followRepository.countByFollower(followerId) );
        }

        @Test @DisplayName("실패: 사용자 없음")
        void getFollowings_userNotFound() {
            given(userRepository.existsById(followerId)).willReturn(false);
            assertThatThrownBy(() -> service.getFollowings(followerId, null, null, 5, null))
                    .isInstanceOf(OtbooException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test @DisplayName("성공: 팔로워 페이징")
        void getFollowers_success() {
            given(userRepository.existsById(followeeId)).willReturn(true);

            Follow f = spy(new Follow(followerId, followeeId));
            UUID mid = UUID.randomUUID();
            doReturn(mid).when(f).getId();

            given(followRepository.findFollowers(eq(followeeId), isNull(), isNull(), any(PageRequest.class)))
                    .willReturn(List.of(f));

            User u1 = mock(User.class, "fUser2"); given(u1.getId()).willReturn(followerId);
            User u2 = mock(User.class, "eUser2"); given(u2.getId()).willReturn(followeeId);
            given(userRepository.findAllById(List.of(followerId, followeeId)))
                    .willReturn(List.of(u1, u2));

            FollowListResponse resp = service.getFollowers(followeeId, null, null, 3, /*nameLike*/null);

            assertThat(resp.data()).hasSize(1);
            assertThat(resp.hasNext()).isFalse();
        }

        @Test @DisplayName("실패: 팔로워 사용자 없음")
        void getFollowers_userNotFound() {
            given(userRepository.existsById(followeeId)).willReturn(false);
            assertThatThrownBy(() -> service.getFollowers(followeeId, null, null, 3, null))
                    .isInstanceOf(OtbooException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("unfollow")
    class Unfollow {

        @Test @DisplayName("성공")
        void success() {
            UUID fid = UUID.randomUUID();
            Follow f = spy(new Follow(followerId, followeeId));
            doReturn(fid).when(f).getId();
            given(followRepository.findById(fid)).willReturn(Optional.of(f));

            service.unfollow(fid, followerId);

            then(followRepository).should().delete(f);
        }

        @Test @DisplayName("실패: 존재하지 않음")
        void notFound() {
            UUID fid = UUID.randomUUID();
            given(followRepository.findById(fid)).willReturn(Optional.empty());
            assertThatThrownBy(() -> service.unfollow(fid, followerId))
                    .isInstanceOf(OtbooException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.FOLLOW_NOT_FOUND);
        }

        @Test @DisplayName("실패: 권한 없음")
        void unauthorized() {
            UUID fid = UUID.randomUUID();
            Follow f = spy(new Follow(followerId, followeeId));
            doReturn(fid).when(f).getId();
            given(followRepository.findById(fid)).willReturn(Optional.of(f));

            assertThatThrownBy(() -> service.unfollow(fid, UUID.randomUUID()))
                    .isInstanceOf(OtbooException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.FOLLOW_UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("createFollow 추가 분기")
    class CreateFollowExtra {

        @Test @DisplayName("실패: 팔로워가 없음")
        void followerNotFound() {
            given(userRepository.existsById(followerId)).willReturn(false);

            assertThatThrownBy(() ->
                    service.createFollow(new FollowCreateRequest(followeeId, followerId))
            )
                    .isInstanceOf(FollowException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test @DisplayName("성공: 알림 전송 중 예외 발생해도 정상 반환")
        void notificationThrows_noFail() {
            FollowCreateRequest req = new FollowCreateRequest(followeeId, followerId);
            given(userRepository.existsById(followerId)).willReturn(true);
            given(userRepository.existsById(followeeId)).willReturn(true);
            given(followRepository.existsByFollowerAndFollowee(followerId, followeeId)).willReturn(false);

            // spy 엔티티 + id stub
            Follow entity = spy(new Follow(followerId, followeeId));
            UUID savedId = UUID.randomUUID();
            doReturn(savedId).when(entity).getId();
            given(followMapper.toEntity(req)).willReturn(entity);
            given(followRepository.save(entity)).willReturn(entity);

            // 유저 스텁
            User uF = mock(User.class); given(uF.getId()).willReturn(followerId);  given(uF.getName()).willReturn("Bob");
            User uE = mock(User.class); given(uE.getId()).willReturn(followeeId);  given(uE.getName()).willReturn("Alice");
            given(userRepository.findAllById(List.of(followerId, followeeId))).willReturn(List.of(uF, uE));

            // toDto 스텁
            FollowDto dto = new FollowDto(
                    savedId,
                    new UserSummary(followeeId, "Alice", null),
                    new UserSummary(followerId,  "Bob",   null)
            );
            given(followMapper.toDto(entity, Map.of(followerId, uF, followeeId, uE))).willReturn(dto);

            // 알림 예외 던지기
            willThrow(new RuntimeException("boom"))
                    .given(notificationService)
                    .createAndSendNotification(any(), any(), any(), any());

            // when
            FollowDto result = service.createFollow(req);

            // then
            assertThat(result).isEqualTo(dto);
        }
    }

    @Nested
    @DisplayName("getFollowSummary 추가 분기")
    class SummaryExtra {

        @Test @DisplayName("성공: 전혀 팔로우 관계 없을 때")
        void none() {
            given(followRepository.countByFollowee(followeeId)).willReturn(0L);
            given(followRepository.countByFollower(followeeId)).willReturn(0L);
            given(followRepository.existsByFollowerAndFollowee(followerId, followeeId)).willReturn(false);
            given(followRepository.existsByFollowerAndFollowee(followeeId, followerId)).willReturn(false);

            FollowSummaryDto dto = service.getFollowSummary(followeeId, followerId);

            assertThat(dto.followerCount()).isZero();
            assertThat(dto.followingCount()).isZero();
            assertThat(dto.followedByMe()).isFalse();
            assertThat(dto.followedByMeId()).isNull();
            assertThat(dto.followingMe()).isFalse();
        }
    }

    @Nested
    @DisplayName("getFollowings / getFollowers 페이징 hasNext=true")
    class ListPagingHasNext {

        @Test @DisplayName("팔로잉 hasNext=true")
        void followingsHasNext() {
            given(userRepository.existsById(followerId)).willReturn(true);

            Follow f1 = spy(new Follow(followerId, followeeId));
            Follow f2 = spy(new Follow(followerId, followeeId));
            doReturn(UUID.randomUUID()).when(f1).getId();
            doReturn(UUID.randomUUID()).when(f2).getId();

            given(followRepository.findFollowings(
                    eq(followerId), isNull(), isNull(), any(PageRequest.class))
            ).willReturn(List.of(f1, f2));
            given(followRepository.countByFollower(followerId)).willReturn(2L);

            User u1 = mock(User.class); given(u1.getId()).willReturn(followerId);
            User u2 = mock(User.class); given(u2.getId()).willReturn(followeeId);
            given(userRepository.findAllById(List.of(followerId, followeeId)))
                    .willReturn(List.of(u1, u2));

            FollowListResponse resp = service.getFollowings(followerId, null, null, 1, null);

            assertThat(resp.hasNext()).isTrue();
            assertThat(resp.nextCursor()).isEqualTo(f1.getId().toString());
            assertThat(resp.nextIdAfter()).isEqualTo(f1.getId());
        }

        @Test @DisplayName("팔로워 hasNext=true")
        void followersHasNext() {
            given(userRepository.existsById(followeeId)).willReturn(true);

            Follow f1 = spy(new Follow(followerId, followeeId));
            Follow f2 = spy(new Follow(followerId, followeeId));
            doReturn(UUID.randomUUID()).when(f1).getId();
            doReturn(UUID.randomUUID()).when(f2).getId();

            given(followRepository.findFollowers(
                    eq(followeeId), isNull(), isNull(), any(PageRequest.class))
            ).willReturn(List.of(f1, f2));
            given(followRepository.countByFollowee(followeeId)).willReturn(2L);

            User u1 = mock(User.class); given(u1.getId()).willReturn(followerId);
            User u2 = mock(User.class); given(u2.getId()).willReturn(followeeId);
            given(userRepository.findAllById(List.of(followerId, followeeId)))
                    .willReturn(List.of(u1, u2));

            FollowListResponse resp = service.getFollowers(followeeId, null, null, 1, null);

            assertThat(resp.hasNext()).isTrue();
            assertThat(resp.nextCursor()).isEqualTo(f1.getId().toString());
            assertThat(resp.nextIdAfter()).isEqualTo(f1.getId());
        }
    }
}
