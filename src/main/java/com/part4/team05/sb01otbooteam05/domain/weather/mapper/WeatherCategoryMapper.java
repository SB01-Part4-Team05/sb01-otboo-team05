package com.part4.team05.sb01otbooteam05.domain.weather.mapper;

import com.part4.team05.sb01otbooteam05.domain.weather.entity.PrecipitationType;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.SkyStatusType;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.WindSpeedAsWord;
import com.part4.team05.sb01otbooteam05.domain.weather.exception.InvalidDataException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WeatherCategoryMapper {
  //변환 값은 기상청 단기예보 조회서비스 오픈 API 활용 가이드를 참고하였음

  //하늘 상태 변환
  public static SkyStatusType toSkyStatusType(String skyCode) {
    return switch (skyCode) {
      case "1" -> SkyStatusType.CLEAR;
      case "3" -> SkyStatusType.MOSTLY_CLOUDY;
      case "4" -> SkyStatusType.CLOUDY;
      default -> {
        log.info("[SkyStatusType] 알 수 없는 코드값: {}", skyCode);
        throw new InvalidDataException("알수없는 코드값: " + skyCode);
      }
    };
  }

  //강수 형태 변환
  public static PrecipitationType toPrecipitationType(String ptyCode) {
    return switch (ptyCode) {
      case "0" -> PrecipitationType.NONE;
      case "1" -> PrecipitationType.RAIN;
      case "2" -> PrecipitationType.RAIN_SNOW;
      case "3" -> PrecipitationType.SNOW;
      case "4" -> PrecipitationType.SHOWER;
      default -> {
        log.info("[PrecipitationType] 알 수 없는 코드값: {}", ptyCode);
        throw new InvalidDataException("알수없는 코드값: " + ptyCode);
      }
    };
  }

  // 강수량 변환
  public static double toPrecipitationAmount(String pcpCode) {
    try {
      if (pcpCode.equals("강수없음")) {
        return 0.0;
      }

      if (pcpCode.equals("1mm 미만")) {
        return 0.5;
      }

      if (pcpCode.endsWith("mm 이상")) {
        pcpCode = pcpCode.replace("mm 이상", "").trim();
      }

      if (pcpCode.endsWith("mm")) {
        pcpCode = pcpCode.replace("mm", "").trim();
      }

      return Double.parseDouble(pcpCode);

    } catch (NumberFormatException e) {
      log.info("[PrecipitationAmount] 알 수 없는 코드값: {}", pcpCode);
      throw new InvalidDataException("알수없는 코드값: " + pcpCode);
    }
  }

  // 강수확률 변환
  public static double toPrecipitationProbability(String popCode) {
    try {
      return Double.parseDouble(popCode) / 100.0;
    } catch (NumberFormatException e) {
      log.info("[PrecipitationProbability] 알 수 없는 코드값: {}", popCode);
      throw new InvalidDataException("알수없는 코드값: " + popCode);
    }
  }

  //풍속 상태 변환
  public static WindSpeedAsWord toWindSpeedAsWord(String wsdCode) {
    try {
      double windSpeed = Double.parseDouble(wsdCode);

      if (windSpeed < 4.0) {
        return WindSpeedAsWord.WEAK;
      } else if (windSpeed < 9.0) {
        return WindSpeedAsWord.MODERATE;
      } else {
        return WindSpeedAsWord.STRONG;
      }
    } catch (NumberFormatException e) {
      log.info("[WindSpeedAsWord] 알 수 없는 코드값: {}", wsdCode);
      throw new InvalidDataException("알수없는 코드값: " + wsdCode);
    }
  }
}
