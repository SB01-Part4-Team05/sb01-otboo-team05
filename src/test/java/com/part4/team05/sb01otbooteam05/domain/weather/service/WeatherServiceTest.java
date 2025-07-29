package com.part4.team05.sb01otbooteam05.domain.weather.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.part4.team05.sb01otbooteam05.domain.user.service.KakaoApiService;
import com.part4.team05.sb01otbooteam05.domain.weather.client.WeatherApiClient;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.ParsedForecastDto;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherAPILocation;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherDto;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import com.part4.team05.sb01otbooteam05.domain.weather.repository.WeatherRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class WeatherServiceTest {

  @Mock
  private WeatherApiClient weatherApiClient;

  @Mock
  private WeatherRepository weatherRepository;

  @Mock
  private KakaoApiService kakaoApiService;

  @InjectMocks
  private WeatherService weatherService;

  private final int x = 60;
  private final int y = 127;
  private final LocalDateTime forecastedAt = LocalDateTime.of(2025, 7, 27, 14, 0);

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }


  @Test
  void generateWeather() {
    int x = 60;
    int y = 127;
    LocalDateTime forecastedAt = LocalDateTime.now();
    Map<LocalDateTime, Map<String, String>> forecastMap = new HashMap<>();
    Map<String, String> data = Map.of(
        "TMP", "25",
        "REH", "60",
        "SKY", "1",
        "PTY", "0",
        "PCP", "강수없음",
        "POP", "10",
        "WSD", "1.5"
    );
    forecastMap.put(forecastedAt.plusHours(1), data);

    ParsedForecastDto parsedForecastDto = new ParsedForecastDto(forecastedAt, forecastMap);
    when(weatherApiClient.fetchForecast(x, y)).thenReturn(parsedForecastDto);

    List<Weather> result = weatherService.generateWeather(x, y);

    assertThat(result).isNotEmpty();
    assertThat(result.get(0).getLocationX()).isEqualTo(x);
    assertThat(result.get(0).getLocationY()).isEqualTo(y);
    assertThat(result.get(0).getTemperatureCurrent()).isEqualTo(25);
    assertThat(result.get(0).getHumidityCurrent()).isEqualTo(60);
  }

  @Test
  void existWeather() {
    when(weatherRepository.existsByLocationXAndLocationYAndForecastedAt(x, y, forecastedAt))
        .thenReturn(true);

    boolean exists = weatherService.existWeather(x, y, forecastedAt);

    assertThat(exists).isTrue();
  }


  @Test
  void getWeatherEntityByIdOrThrow() {
    UUID id = UUID.randomUUID();
    Weather weather = Weather.builder().id(id).build();
    when(weatherRepository.findById(id)).thenReturn(Optional.of(weather));

    Weather result = weatherService.getWeatherEntityByIdOrThrow(id);

    assertThat(result.getId()).isEqualTo(id);
  }

  @Test
  void findExistingWeatherLocations() {
    LocalDateTime forecastedAt = LocalDateTime.now();
    List<Object[]> mockResult = List.of(new Object[]{60, 127}, new Object[]{61, 128});
    when(weatherRepository.findLocationsByForecastedAt(forecastedAt)).thenReturn(mockResult);

    Set<Pair<Integer, Integer>> locations = weatherService.findExistingWeatherLocations(forecastedAt);

    assertThat(locations).containsExactlyInAnyOrder(
        Pair.of(60, 127),
        Pair.of(61, 128)
    );
  }

  @Test
  void getWeatherAPILocationTest() {
    // given
    double latitude = 37.5665;
    double longitude = 126.9780;
    when(kakaoApiService.getLocationNames(anyDouble(), anyDouble()))
        .thenReturn(List.of("서울특별시", "중구"));

    // when
    WeatherAPILocation result = weatherService.getWeatherAPILocation(longitude, latitude);

    // then
    assertThat(result.latitude()).isEqualTo(latitude);
    assertThat(result.longitude()).isEqualTo(longitude);
    assertThat(result.x()).isNotNull();
    assertThat(result.y()).isNotNull();
    assertThat(result.locationNames()).contains("서울특별시");
  }


  @Test
  void getWeathersTest() {
    // given
    double lat = 37.5665;
    double lon = 126.9780;
    int x = 59;
    int y = 126;

    LocalDateTime now = LocalDateTime.now();
    LocalTime requestedTime = now.toLocalTime()
        .plusHours(1)
        .truncatedTo(java.time.temporal.ChronoUnit.HOURS)
        .withSecond(0).withNano(0);

    List<LocalDateTime> forecastAtList = new ArrayList<>();
    for (int i = 0; i <= 4; i++) {
      LocalDate targetDate = now.toLocalDate().plusDays(i);
      LocalTime targetTime = i < 3 ? requestedTime : LocalTime.MIDNIGHT;
      forecastAtList.add(LocalDateTime.of(targetDate, targetTime).withSecond(0).withNano(0));
    }

    Weather weather = Weather.builder()
        .id(UUID.randomUUID())
        .locationX(x)
        .locationY(y)
        .forecastAt(forecastAtList.get(0))
        .forecastedAt(now)
        .temperatureCurrent(25.0)
        .humidityCurrent(60.0)
        .build();

    when(kakaoApiService.getLocationNames(lat, lon)).thenReturn(List.of("서울"));
    when(weatherRepository.findLatestByLocationAndForecastAtIn(eq(x), eq(y), anyList()))
        .thenReturn(List.of(weather));

    // when
    List<WeatherDto> result = weatherService.getWeathers(lon, lat);

    // then
    assertThat(result).isNotEmpty();
    assertThat(result.get(0).temperature().current()).isEqualTo(25.0);
    assertThat(result.get(0).location().locationNames()).contains("서울");
  }


}
