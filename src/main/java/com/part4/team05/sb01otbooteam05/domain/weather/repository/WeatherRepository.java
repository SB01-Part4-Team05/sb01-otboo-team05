package com.part4.team05.sb01otbooteam05.domain.weather.repository;

import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherRepository extends JpaRepository<Weather, UUID> {

  boolean existsByLocationXAndLocationY(Integer locationX, Integer locationY);
}
