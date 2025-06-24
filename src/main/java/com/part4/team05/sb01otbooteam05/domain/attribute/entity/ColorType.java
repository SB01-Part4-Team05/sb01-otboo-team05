package com.part4.team05.sb01otbooteam05.domain.attribute.entity;

public enum ColorType {
  WHITE("화이트"),
  GRAY("그레이"),
  BLACK("블랙"),
  BURGUNDY("버건디"),
  PINK("핑크"),
  CREAM("크림");

  private final String label;

  ColorType(String label){
    this.label = label;
  }

  public String getLabel(){
    return label;
  }
}
