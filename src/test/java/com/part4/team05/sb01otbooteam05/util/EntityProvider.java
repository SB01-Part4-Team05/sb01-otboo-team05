package com.part4.team05.sb01otbooteam05.util;

import com.part4.team05.sb01otbooteam05.domain.attribute.entity.*;
import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.ClothesType;
import com.part4.team05.sb01otbooteam05.domain.feed.entity.Feed;
import com.part4.team05.sb01otbooteam05.domain.feedLike.entity.FeedLike;
import com.part4.team05.sb01otbooteam05.domain.ootd.entity.Ootd;
import com.part4.team05.sb01otbooteam05.domain.user.entity.GenderType;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.entity.UserRole;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherAPILocation;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.PrecipitationType;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.SkyStatusType;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.WindSpeedAsWord;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Profile("test")
@RequiredArgsConstructor
public class EntityProvider {

    private final List<AttributeDefinition> attributeDefinitionList = List.of(
            new AttributeDefinition(UUID.randomUUID(), "색", Arrays.stream(ColorType.values()).map(color -> color.getLabel()).toList()),
            new AttributeDefinition(UUID.randomUUID(), "사이즈", Arrays.stream(SizeType.values()).map(size -> size.name()).toList()),
            new AttributeDefinition(UUID.randomUUID(), "스타일", Arrays.stream(StyleType.values()).map(style -> style.getLabel()).toList()),
            new AttributeDefinition(UUID.randomUUID(), "두께", Arrays.stream(ThicknessType.values()).map(thickness -> thickness.getLabel()).toList()),
            new AttributeDefinition(UUID.randomUUID(), "질감", Arrays.stream(TouchType.values()).map(touchType -> touchType.getLabel()).toList()));

    private List<User> buildTestUser(int userNumber) {
        ArrayList<User> testUsers = new ArrayList<>();
        for (int i = 0; i < userNumber; i++) {
            String randomString = Integer.toString(randomInt(1, 99999));
            User testUser = User.builder()
                    .name("user" + randomString)
                    .email("test" + randomString + "@email.com")
                    .role(UserRole.USER)
                    .password("password486!!")
                    .locked(false)
                    .build();
            ReflectionTestUtils.setField(testUser, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(testUser, "createdAt", randomDateGenerator(LocalDate.of(1950, 1, 1), LocalDate.of(2010, 12, 31)));
            testUsers.add(testUser);
        }
        return testUsers;
    }

    public List<User> createTestUsers(int userNumber) {
        return buildTestUser(userNumber);
    }

    public User createTestUserWithLocationData() {
        UUID randomUuid = UUID.randomUUID();
        String randomString = randomUuid.toString().substring(0, 5);


        User testUser = User.builder()
                .gender((randomInt(0, 1) == 1) ? GenderType.FEMALE : GenderType.MALE)
                .locked(false)
                .name("user" + randomString)
                .email("test" + randomString + "@email.com")
                .password("password486!!")
                .role(UserRole.USER)
                .birthDate(randomDateGenerator(LocalDate.of(1970, 1, 1), LocalDate.of(2010, 1, 1)))
                .latitude(randomDouble(0.0, 100.0))
                .longitude(randomDouble(0.0, 100.0))
                .locationX(randomInt(0, 100))
                .locationY(randomInt(0, 100))
                .profileImageUrl(null)
                .locationNames(null)
                .temperatureSensitivity(randomInt(0, 5))
                .isTempPassword(false)
                .passwordExpiresAt(null)
                .provider("KAKAO")
                .providerId(UUID.randomUUID().toString())
                .locationNames(Collections.emptyList())
                .build();

        ReflectionTestUtils.setField(testUser, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(testUser, "createdAt", randomDateGenerator(LocalDate.of(1950, 1, 1), LocalDate.of(2010, 12, 31)));
        return testUser;
    }

    public CustomUserDetails createCustomUserDetailsById(UUID userId) {
        return new CustomUserDetails(userId, userId.toString().substring(0, 5) + "@email.com", UserRole.USER.name());
    }


    public Weather createTestWeather() {
        LocalDateTime now = LocalDateTime.now();

        Weather testWeather = Weather.builder()
                .locationX(randomInt(0, 100)) // 0~100 사이 랜덤 좌표
                .locationY(randomInt(0, 100))
                .forecastedAt(now.minusHours(randomInt(0, 12))) // 예보된 시각
                .forecastAt(now.plusHours(randomInt(1, 24)))    // 예보 시간
                .skyStatusType(randomEnum(SkyStatusType.class))
                .precipitationType(randomEnum(PrecipitationType.class))
                .precipitationAmount(randomDouble(0.0, 100.0))
                .precipitationProbability(randomDouble(0.0, 100.0))
                .humidityCurrent(randomDouble(10.0, 90.0))
                .humidityComparedToDayBefore(randomDouble(-20.0, 20.0))
                .temperatureCurrent(randomDouble(-10.0, 35.0))
                .temperatureComparedToDayBefore(randomDouble(-5.0, 5.0))
                .temperatureMin(randomDouble(-15.0, 20.0))
                .temperatureMax(randomDouble(20.0, 40.0))
                .windSpeed(randomDouble(0.0, 20.0))
                .windSpeedAsWord(randomEnum(WindSpeedAsWord.class))
                .build();

        ReflectionTestUtils.setField(testWeather, "id", UUID.randomUUID());
        return testWeather;
    }

    private List<Feed> buildTestFeeds(int feedNumber, List<User> authors, Weather weather) {
        ArrayList<Feed> testFeeds = new ArrayList<>();
        for (int i = 0; i < feedNumber; i++) {
            Feed testFeed = new Feed(authors.get(randomInt(0, authors.size() - 1)), weather, "테스트피드입니다.");
            ReflectionTestUtils.setField(testFeed, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(testFeed, "createdAt", randomDateGenerator(LocalDate.of(2000, 1, 1), LocalDate.now()));
            testFeeds.add(testFeed);
        }
        return testFeeds;
    }

    private List<Ootd> buildTestOotds(List<Clothes> clothesList, Feed feed) {
        List<Ootd> ootds = clothesList.stream().map(clothes -> new Ootd(feed, clothes)).toList();
        ootds.stream().forEach(ootd -> ReflectionTestUtils.setField(ootd, "id", UUID.randomUUID()));
        return ootds;
    }

    public Feed createTestFeed(User author, List<Clothes> clothesList, Weather weather) {
        Feed testFeed = buildTestFeeds(1, List.of(author), weather).get(0);
        List<Ootd> ootds = buildTestOotds(clothesList, testFeed);
        return testFeed;
    }

    public List<Feed> createTestFeeds(int feedNumber, List<User> authors, List<Clothes> clothesList, Weather weather) {
        List<Feed> feeds = buildTestFeeds(feedNumber, authors, weather);
        List<Ootd> ootds = feeds.stream().map(testFeed -> buildTestOotds(clothesList, testFeed)).flatMap(List::stream).toList();
        return feeds;
    }

    public Clothes createTestClothes(User owner, String imageUrl) {

        Clothes testClothes = Clothes.builder()
                //.id(UUID.randomUUID())
                .type(randomEnum(ClothesType.class))
                .ownerId(owner.getId())
                .name("user" + randomInt(0, 999))
                .imageUrl(imageUrl)
                .attributeValues(Collections.emptyList())
                .build();
        ReflectionTestUtils.setField(testClothes, "id", UUID.randomUUID());

        // AttributeValue 객체 내 value 필드가 h2 db랑 충돌해서, 내부값은 넣지 않음.
        /*testClothes.setAttributeValues(attributeDefinitionList.stream().map(attDef ->
                new AttributeValue(((long) attributeDefinitionList.indexOf(attDef)),
                        randomString(attDef.getSelectableValues()),
                        testClothes,
                        attDef)
        ).toList());*/
        return testClothes;
    }

    public FeedLike createTestFeedLike(User user, Feed feed) {
        FeedLike testFeedLike = FeedLike.builder()
                .feed(feed)
                .author(user)
                .build();
        //todo 생성일수정일 안넣은상태
        ReflectionTestUtils.setField(testFeedLike, "id", UUID.randomUUID());
        feed.setLikeCount(feed.getLikeCount() + 1);
        return testFeedLike;
    }

    public WeatherAPILocation createTestWeatherAPILocation(User user) {
        return new WeatherAPILocation(user.getLatitude(), user.getLongitude(), user.getLocationX(), user.getLocationY(), List.of("testLocation"));
    }

    public LocalDate randomDateGenerator(LocalDate startInclusive, LocalDate endExclusive) {
        long startEpoch = startInclusive.toEpochDay();
        long endEpoch = endExclusive.toEpochDay();

        long randomEpoch = ThreadLocalRandom.current().nextLong(startEpoch, endEpoch);
        return LocalDate.ofEpochDay(randomEpoch);
    }

    private int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private double randomDouble(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    private String randomString(List<String> inputList) {
        return inputList.get(ThreadLocalRandom.current().nextInt(inputList.size()));
    }

    private <T extends Enum<?>> T randomEnum(Class<T> enumClass) {
        T[] enumConstants = enumClass.getEnumConstants();
        return enumConstants[ThreadLocalRandom.current().nextInt(enumConstants.length)];
    }


}
