package com.part4.team05.sb01otbooteam05.domain.weather.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.part4.team05.sb01otbooteam05.domain.weather.entity.PrecipitationType;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import com.part4.team05.sb01otbooteam05.domain.weather.service.WeatherNotificationService.WeatherChangedType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeatherNotificationServiceTest {

  private WeatherNotificationService weatherNotificationService;

  @BeforeEach
  void setUp() {
    weatherNotificationService = new WeatherNotificationService(null, null, null);
  }

  //
  @Test
  void detectChangedTypes_TEMP_CHANGE() {
    //given
    Weather previous = Weather.builder()
        .temperatureCurrent(10.0)
        .precipitationType(PrecipitationType.NONE)
        .build();

    Weather latest = Weather.builder()
        .temperatureCurrent(16.0)
        .precipitationType(PrecipitationType.NONE)
        .build();
    //when
    List<WeatherChangedType> result = weatherNotificationService.detectChangedTypes(latest, previous);

    //then
    assertThat(result).containsExactly(WeatherChangedType.TEMP_CHANGE);
  }

  @Test
  void detectChangedTypes_RAIN_SNOW_STARTED() {
    //given
    Weather previous = Weather.builder()
        .temperatureCurrent(10.0)
        .precipitationType(PrecipitationType.NONE)
        .build();
    Weather latest = Weather.builder()
        .temperatureCurrent(10.0)
        .precipitationType(PrecipitationType.RAIN)
        .build();

    //when
    List<WeatherChangedType> result = weatherNotificationService.detectChangedTypes(latest, previous);

    //then
    assertThat(result).containsExactly(WeatherChangedType.RAIN_SNOW_STARTED);
  }

  @Test
  void detectChangedTypes_TEMP_CHANGE_RAIN_SNOW_STARTED() {
    //given
    Weather previous = Weather.builder()
        .temperatureCurrent(10.0)
        .precipitationType(PrecipitationType.NONE)
        .build();

    Weather latest = Weather.builder()
        .temperatureCurrent(16.0)
        .precipitationType(PrecipitationType.RAIN)
        .build();

    //when
    List<WeatherChangedType> result = weatherNotificationService.detectChangedTypes(latest, previous);

    //then
    assertThat(result).containsExactlyInAnyOrder(
        WeatherChangedType.TEMP_CHANGE,
        WeatherChangedType.RAIN_SNOW_STARTED
    );
  }


  @Test
  void detectChangedTypes_None() {
    //given
    Weather previous = Weather.builder()
        .temperatureCurrent(10.0)
        .precipitationType(PrecipitationType.NONE)
        .build();

    Weather latest = Weather.builder()
        .temperatureCurrent(11.0)
        .precipitationType(PrecipitationType.NONE)
        .build();

    //when
    List<WeatherChangedType> result = weatherNotificationService.detectChangedTypes(latest, previous);

    //then
    assertThat(result).isEmpty();
  }
}

