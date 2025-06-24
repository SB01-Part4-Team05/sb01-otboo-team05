package com.part4.team05.sb01otbooteam05.domain.clothes.entity;

public enum ClothesType{
  TOP("상의"),
  BOTTOM("하의"),
  DRESS("원피스"),
  OUTER("아우터"),
  UNDERWEAR("속옷"),
  ACC("액세서리"),
  SHOES("신발"),
  SOCKS("양말"),
  CAP("모자"),
  BAG("가방"),
  SCARF("스카프");

  private final String label;

  ClothesType(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
