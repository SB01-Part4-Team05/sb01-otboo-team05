package com.part4.team05.sb01otbooteam05.domain.attribute.controller;

import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeDefinition;
import com.part4.team05.sb01otbooteam05.domain.attribute.service.AttributeService;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesAttributeDefCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesAttributeDefUpdateRequest;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clothes/attribute-defs")
public class AttributeController {
  private final AttributeService service;

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<AttributeDefinition> createDef(ClothesAttributeDefCreateRequest request){
    return ResponseEntity.status(HttpStatus.CREATED).body(service.createDef(request));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{definitionId}")
  public ResponseEntity<AttributeDefinition> update(@PathVariable UUID definitionId, @RequestBody ClothesAttributeDefUpdateRequest request){
    return ResponseEntity.ok(service.updateDef(definitionId,request));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{definitionId}")
  public ResponseEntity<Void> deleteDef(@PathVariable UUID definitionId){
    service.deleteDef(definitionId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<List<AttributeDefinition>> getDef(@RequestParam int limit){
    return ResponseEntity.ok(service.getDef(limit));
  }
}
