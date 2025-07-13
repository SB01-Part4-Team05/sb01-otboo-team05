package com.part4.team05.sb01otbooteam05.domain.weather.controller;

import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherAPILocation;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherDto;
import com.part4.team05.sb01otbooteam05.domain.weather.service.WeatherService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weathers")
public class WeatherController {

  private final WeatherService weatherService;

  @GetMapping
  public ResponseEntity<List<WeatherDto>> getWeathers(
      @RequestParam double longitude,
      @RequestParam double latitude
  ) {
    log.info("날씨 정보 조회 longitude = {}, latitude = {}", longitude, latitude);
    return ResponseEntity.ok(weatherService.getWeathers(longitude, latitude));
  }

  @GetMapping("/locations")
  public ResponseEntity<WeatherAPILocation> getWeatherLocation(
      @RequestParam double longitude,
      @RequestParam double latitude
  ) {
    log.info("날씨 위치 정보 조회 longitude = {}, latitude = {}", longitude, latitude);
    return ResponseEntity.ok(weatherService.getWeatherAPILocation(longitude, latitude));
  }

}
