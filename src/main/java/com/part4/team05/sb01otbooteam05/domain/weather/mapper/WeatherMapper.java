package com.part4.team05.sb01otbooteam05.domain.weather.mapper;

import com.part4.team05.sb01otbooteam05.domain.weather.dto.HumidityDto;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.PrecipitationDto;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.TemperatureDto;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherAPILocation;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherDto;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WeatherSummaryDto;
import com.part4.team05.sb01otbooteam05.domain.weather.dto.WindSpeedDto;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WeatherMapper {

	public static WeatherDto toDto(Weather weather, WeatherAPILocation weatherAPILocation) {
		return new WeatherDto(
			weather.getId(),
			weather.getForecastedAt(),
			weather.getForecastAt(),
			weatherAPILocation,
			weather.getSkyStatusType(),
			toPrecipitationDto(weather),
			toHumidityDto(weather),
			toTemperatureDto(weather),
			toWindSpeedDto(weather)
		);
	}

	public static WeatherSummaryDto toSummaryDto(Weather weather) {
		return new WeatherSummaryDto(
			weather.getId(),
			weather.getSkyStatusType(),
			toPrecipitationDto(weather),
			toTemperatureDto(weather)
		);
	}

	public static WindSpeedDto toWindSpeedDto(Weather weather) {
		return new WindSpeedDto(
			weather.getWindSpeed(),
			weather.getWindSpeedAsWord()
		);
	}

	public static TemperatureDto toTemperatureDto(Weather weather) {
		return new TemperatureDto(
			weather.getTemperatureCurrent(),
			weather.getTemperatureComparedToDayBefore(),
			weather.getTemperatureMin(),
			weather.getTemperatureMax()
		);
	}

	public static PrecipitationDto toPrecipitationDto(Weather weather) {
		return new PrecipitationDto(
			weather.getPrecipitationType(),
			weather.getPrecipitationAmount(),
			weather.getPrecipitationProbability()
		);
	}

	public static HumidityDto toHumidityDto(Weather weather) {
		return new HumidityDto(
			weather.getHumidityCurrent(),
			weather.getHumidityComparedToDayBefore()
		);
	}
}
