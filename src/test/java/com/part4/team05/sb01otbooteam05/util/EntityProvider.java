package com.part4.team05.sb01otbooteam05.util;

import com.part4.team05.sb01otbooteam05.domain.attribute.entity.*;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.ClothesType;
import com.part4.team05.sb01otbooteam05.domain.user.entity.GenderType;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.entity.UserRole;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.PrecipitationType;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.SkyStatusType;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.WindSpeedAsWord;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class EntityProvider {

    private final List<AttributeDefinition> attributeDefinitionList = List.of(
            new AttributeDefinition(UUID.randomUUID(), "색", Arrays.stream(ColorType.values()).map(color -> color.getLabel()).toList()),
            new AttributeDefinition(UUID.randomUUID(), "사이즈", Arrays.stream(SizeType.values()).map(size -> size.name()).toList()),
            new AttributeDefinition(UUID.randomUUID(), "스타일", Arrays.stream(StyleType.values()).map(style -> style.getLabel()).toList()),
            new AttributeDefinition(UUID.randomUUID(), "두께", Arrays.stream(ThicknessType.values()).map(thickness -> thickness.getLabel()).toList()),
            new AttributeDefinition(UUID.randomUUID(), "질감", Arrays.stream(TouchType.values()).map(touchType -> touchType.getLabel()).toList()));

    public static User createTestUserWithLocationData() {
        UUID randomUuid = UUID.randomUUID();
        String randomString = randomUuid.toString().substring(0, 5);

        return User.builder()
                .gender((randomInt(0, 1) == 1) ? GenderType.FEMALE : GenderType.MALE)
                .locked(false)
                .name("user" + randomString)
                .email("test" + randomString + "@email.com").password("password486!!")
                .role(UserRole.USER).birthDate(randomDateGenerator(LocalDate.of(1970, 1, 1), LocalDate.of(2010, 1, 1)))
                .latitude(randomDouble(0.0, 100.0))
                .longitude(randomDouble(0.0, 100.0))
                .locationX(randomInt(0, 100)) // 위도 경도는 현재시간으로 대충 설정함.
                .locationY(randomInt(0, 100))
                .profileImageUrl(null)
                .locationNames(null)
                .temperatureSensitivity(randomInt(0, 5))
                .isTempPassword(false)
                .passwordExpiresAt(null)
                .build();
    }


    public static Weather createTestWeather() {
        LocalDateTime now = LocalDateTime.now();

        return Weather.builder()
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
    }

    public static LocalDate randomDateGenerator(LocalDate startInclusive, LocalDate endExclusive) {
        long startEpoch = startInclusive.toEpochDay();
        long endEpoch = endExclusive.toEpochDay();

        long randomEpoch = ThreadLocalRandom.current().nextLong(startEpoch, endEpoch);
        return LocalDate.ofEpochDay(randomEpoch);
    }

    private static int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private static double randomDouble(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    private static String randomString(List<String> inputList) {
        return inputList.get(ThreadLocalRandom.current().nextInt(inputList.size()));
    }

    private static <T extends Enum<?>> T randomEnum(Class<T> enumClass) {
        T[] enumConstants = enumClass.getEnumConstants();
        return enumConstants[ThreadLocalRandom.current().nextInt(enumConstants.length)];
    }

    public Clothes createTestClothes(User owner, String imageUrl) {
        UUID uuid = UUID.randomUUID();

        Clothes clothes = Clothes.builder()
                .id(UUID.randomUUID())
                .type(randomEnum(ClothesType.class))
                .ownerId(owner.getId())
                .name("user" + uuid.toString().substring(0, 5))
                .imageUrl(imageUrl)
                .attributeValues(Collections.emptyList())
                .build();

        clothes.setAttributeValues(attributeDefinitionList.stream().map(attDef ->
                new AttributeValue(((long) attributeDefinitionList.indexOf(attDef)),
                        randomString(attDef.getSelectableValues()),
                        clothes,
                        attDef)
        ).toList());
        return clothes;
    }

}