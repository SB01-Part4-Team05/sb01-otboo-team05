package com.part4.team05.sb01otbooteam05.domain.weather.mapper;

import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import java.sql.Timestamp;

public class WeatherJdbcMapper {

  public static Object[] toJdbcRow(Weather weather){
    return new Object[] {
        weather.getId(),
        weather.getLocationX(),
        weather.getLocationY(),
        Timestamp.valueOf(weather.getForecastedAt()),
        Timestamp.valueOf(weather.getForecastAt()),
        weather.getSkyStatusType().name(),
        weather.getPrecipitationType().name(),
        weather.getPrecipitationAmount(),
        weather.getPrecipitationProbability(),
        weather.getHumidityCurrent(),
        weather.getHumidityComparedToDayBefore(),
        weather.getTemperatureCurrent(),
        weather.getTemperatureComparedToDayBefore(),
        weather.getTemperatureMin(),
        weather.getTemperatureMax(),
        weather.getWindSpeed(),
        weather.getWindSpeedAsWord().name()
    };
  }

}
