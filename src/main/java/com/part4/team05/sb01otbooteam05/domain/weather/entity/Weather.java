package com.part4.team05.sb01otbooteam05.domain.weather.entity;

import com.part4.team05.sb01otbooteam05.domain.base.BaseEntity;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@AllArgsConstructor
@Table(name = "weathers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Weather extends BaseEntity {

  @Column(name = "location_x", nullable = false)
  private Integer locationX;

  @Column(name = "location_y", nullable = false)
  private Integer locationY;

  @Column(name = "forecasted_at", nullable = false)
  private LocalDateTime forecastedAt;

  @Column(name = "forecast_at", nullable = false)
  private LocalDateTime forecastAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "sky_status", nullable = false)
  private SkyStatus skyStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "precipitation_type", nullable = false)
  private PrecipitationType precipitationType;

  @Column(name = "precipitation_amount", nullable = false)
  private Double precipitationAmount;

  @Column(name = "precipitation_probability", nullable = false)
  private Double precipitationProbability;

  @Column(name = "humidity_current", nullable = false)
  private Double humidityCurrent;

  @Column(name = "humidity_compared_to_day_before")
  private Double humidityComparedToDayBefore;

  @Column(name = "temperature_current", nullable = false)
  private Double temperatureCurrent;

  @Column(name = "temperature_compared_to_day_before")
  private Double temperatureComparedToDayBefore;

  @Column(name = "temperature_min", nullable = false)
  private Double temperatureMin;

  @Column(name = "temperature_max", nullable = false)
  private Double temperatureMax;

  @Column(name = "wind_speed", nullable = false)
  private Double windSpeed;

  @Enumerated(EnumType.STRING)
  @Column(name = "wind_speed_as_word", nullable = false)
  private WindSpeedAsWord windSpeedAsWord;

}
