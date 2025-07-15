package com.part4.team05.sb01otbooteam05.domain.weather.repository;

import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherRepository extends JpaRepository<Weather, UUID> {

  Optional<Weather> findByLocationXAndLocationYAndForecastAt(Integer locationX, Integer locationY, LocalDateTime forecastAt);

  boolean existsByLocationXAndLocationYAndForecastedAt(Integer locationX, Integer locationY, LocalDateTime forecastedAt);

  boolean existsByLocationXAndLocationY(Integer locationX, Integer locationY);

  List<Weather> findByLocationXAndLocationYAndForecastAtIn(Integer locationX, Integer locationY, Collection<LocalDateTime> forecastAts);
}
