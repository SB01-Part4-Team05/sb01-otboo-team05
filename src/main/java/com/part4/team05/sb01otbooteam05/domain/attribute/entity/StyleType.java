package com.part4.team05.sb01otbooteam05.domain.attribute.entity;

public enum StyleType {
  CASUAL("캐주얼"),
  STREET("스트릿"),
  SPORTY("스포티"),
  MINIMAL("미니멀"),
  CHIC("시크"),
  RETRO("레트로");

  private final String label;

   StyleType(String label){
     this.label = label;
   }

   public String getLabel(){
     return label;
   }
}
