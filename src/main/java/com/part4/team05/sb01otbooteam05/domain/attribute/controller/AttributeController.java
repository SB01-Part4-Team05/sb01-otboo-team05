package com.part4.team05.sb01otbooteam05.domain.attribute.controller;

import com.part4.team05.sb01otbooteam05.domain.attribute.entity.Attribute;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeDefinition;
import com.part4.team05.sb01otbooteam05.domain.attribute.service.AttributeService;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesAttributeDefCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesAttributeDefUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clothes/attribute-defs")
public class AttributeController {
  private final AttributeService service;

  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<AttributeDefinition> createDef(ClothesAttributeDefCreateRequest request){
    return ResponseEntity.status(HttpStatus.CREATED).body(service.createDef(request));
  }

  @PreAuthorize("hasAnyRole('ADMIN')")
  public ResponseEntity<AttributeDefinition> update(ClothesAttributeDefUpdateRequest request){
    return ResponseEntity.ok().body(service.updateDef(request));
  }
}
