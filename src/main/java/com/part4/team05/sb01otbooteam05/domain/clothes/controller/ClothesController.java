package com.part4.team05.sb01otbooteam05.domain.clothes.controller;


import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesDto;
import com.part4.team05.sb01otbooteam05.domain.clothes.service.ClothesService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clothes")
public class ClothesController {

  private final ClothesService clothesService;

  @GetMapping
  public ResponseEntity<List<ClothesDto>> getClothes(@RequestParam UUID ownerId){
    return ResponseEntity.ok().body(clothesService.get(ownerId));
  }

  @PostMapping
  public ResponseEntity<ClothesDto> saveClothes(@RequestBody ClothesCreateRequest request){
    return ResponseEntity.status(HttpStatus.CREATED).body(clothesService.create(request));
  }

  @DeleteMapping("/{clothesId}")
  public ResponseEntity<Void> delete(@PathVariable UUID clothesId){
    clothesService.delete(clothesId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping(value="/{clothesId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ClothesDto> patchClothes(@PathVariable UUID clothesId,
      @RequestPart("request") ClothesUpdateRequest request,
      @RequestPart(required = false, value = "image")MultipartFile image){
    return ResponseEntity.ok().body(clothesService.update(clothesId,request,image));
  }
}
