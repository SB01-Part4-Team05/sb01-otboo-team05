package com.part4.team05.sb01otbooteam05.domain.weather.entity;

public enum PrecipitationType {
  NONE,
  RAIN,
  RAIN_SNOW,
  SNOW,
  SHOWER; //소나기

  public boolean isRainOrSnow() {
    return this == RAIN || this == RAIN_SNOW || this == SNOW || this == SHOWER;
  }
}
