package com.part4.team05.sb01otbooteam05.domain.weather.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeatherResponse {

  private Response response;

  @Getter
  @Setter
  public static class Response {

    private Header header;
    private Body body;
  }

  @Getter
  @Setter
  public static class Header {

    private String resultCode;
    private String resultMsg;
  }

  @Getter
  @Setter
  public static class Body {

    private String dataType;
    private Items items;
  }

  @Getter
  @Setter
  public static class Items {

    private List<Item> item;
  }

  @Getter
  @Setter
  public static class Item {

    private String baseDate;

    private String baseTime;

    private String category;

    private String fcstDate;

    private String fcstTime;

    private String fcstValue;

    private int nx;
    private int ny;

  }

}
