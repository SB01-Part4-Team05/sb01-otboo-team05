package com.part4.team05.sb01otbooteam05.domain.attribute.entity;

public enum ThicknessType {
  THICK("두꺼움"),
  SLIMTHICK("약간두꺼움"),
  SLIMTHIN("약간얇음"),
  THIN("얇음");

  private final String label;

  ThicknessType(String label){
    this.label = label;
  }

  public String getLabel(){
    return label;
  }
}
