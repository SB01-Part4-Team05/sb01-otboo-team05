package com.part4.team05.sb01otbooteam05.domain.attribute.controller;

import com.part4.team05.sb01otbooteam05.domain.attribute.dto.ClothesAttributeDefDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeDefinition;
import com.part4.team05.sb01otbooteam05.domain.attribute.service.AttributeService;
import com.part4.team05.sb01otbooteam05.domain.attribute.dto.ClothesAttributeDefCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.attribute.dto.ClothesAttributeDefUpdateRequest;
import java.util.UUID;

import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.SortDirection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
public class AttributeController implements AttributeControllerDoc{
  private final AttributeService attributeService;

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<AttributeDefinition> createDef(@RequestBody ClothesAttributeDefCreateRequest request,
                                                       @AuthenticationPrincipal CustomUserDetails me){
    return ResponseEntity.status(HttpStatus.CREATED).body(attributeService.createDef(request, me.getUserId()));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{definitionId}")
  public ResponseEntity<AttributeDefinition> update(@PathVariable UUID definitionId, @RequestBody ClothesAttributeDefUpdateRequest request,
                                                    @AuthenticationPrincipal CustomUserDetails me){
    return ResponseEntity.ok(attributeService.updateDef(definitionId,request, me.getUserId()));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{definitionId}")
  public ResponseEntity<Void> deleteDef(@PathVariable UUID definitionId,
                                        @AuthenticationPrincipal CustomUserDetails me){
    attributeService.deleteDef(definitionId, me.getUserId());
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<ClothesAttributeDefDtoCursorResponse> getAttributes(
      @RequestParam(required = false) UUID cursor,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam(required = false, defaultValue = "id") String sortedBy,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(required = false,defaultValue = "") String keywordLike
      ) {

    return ResponseEntity.ok(attributeService.getDef(cursor,size,idAfter,sortedBy,sortDirection,keywordLike));
  }
}
