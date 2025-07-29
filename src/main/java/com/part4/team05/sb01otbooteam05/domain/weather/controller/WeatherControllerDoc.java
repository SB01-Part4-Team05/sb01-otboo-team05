package com.part4.team05.sb01otbooteam05.domain.weather.controller;

import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherAPILocation;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherDto;
import com.part4.team05.sb01otbooteam05.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "날씨 관리", description = "날씨 관련 API")
public interface WeatherControllerDoc {

  @Operation(
      summary = "날씨 정보 조회",
      description = "날씨 정보 조회 API"
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "날씨 조회 성공",
          content = @Content(schema = @Schema(implementation = List.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "날씨 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<List<WeatherDto>> getWeathers(
      @Parameter(description = "longitude") @RequestParam double longitude,
      @Parameter(description = "latitude") @RequestParam double latitude
  );

  @Operation(
      summary = "날씨 위치 정보 조회",
      description = "날씨 위치 정보 조회 API"
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "날씨 위치 정보 조회 성공",
          content = @Content(schema = @Schema(implementation = WeatherAPILocation.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "날씨 위치 정보 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<WeatherAPILocation> getWeatherLocation(
      @Parameter(description = "longitude") @RequestParam double longitude,
      @Parameter(description = "latitude") @RequestParam double latitude
  );
}
