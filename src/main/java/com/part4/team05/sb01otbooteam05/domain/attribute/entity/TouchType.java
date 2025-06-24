package com.part4.team05.sb01otbooteam05.domain.attribute.entity;

public enum TouchType {
  SOFT("부드러움"),
  HARD("뻣뻣함");

  private final String label;

  TouchType(String label){
    this.label = label;
  }

  public String getLabel(){
    return label;
  }
}
