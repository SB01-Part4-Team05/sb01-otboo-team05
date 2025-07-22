package com.part4.team05.sb01otbooteam05.domain.weather.repository;

import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WeatherRepository extends JpaRepository<Weather, UUID> {

  boolean existsByLocationXAndLocationYAndForecastedAt(Integer locationX, Integer locationY, LocalDateTime forecastedAt);

  boolean existsByLocationXAndLocationY(Integer locationX, Integer locationY);

  @Query(value = """
    SELECT DISTINCT ON (w.forecast_at) *
    FROM weathers w
    WHERE w.location_x = :x
    AND w.location_y = :y
    AND w.forecast_at IN (:forecastAts)
    ORDER BY w.forecast_at, w.forecasted_at DESC
    """, nativeQuery = true)
  List<Weather> findLatestByLocationAndForecastAtIn(
      @Param("x") Integer x,
      @Param("y") Integer y,
      @Param("forecastAts") Collection<LocalDateTime> forecastAts
  );

  List<Weather> findByForecastAt(LocalDateTime forecastAt);
}
