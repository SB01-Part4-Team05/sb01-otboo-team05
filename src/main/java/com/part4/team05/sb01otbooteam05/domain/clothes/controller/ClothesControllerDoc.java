package com.part4.team05.sb01otbooteam05.domain.clothes.controller;

import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesDto;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesUpdateRequest;
import com.part4.team05.sb01otbooteam05.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "의상 관리", description = "의상 관련 API")
public interface ClothesControllerDoc {

  @Operation(
      summary = "옷 목록 조회",
      description = "옷 목록 조회 API"
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "옷 목록 조회 성공",
          content = @Content(schema = @Schema(implementation = ClothesCursorResponse.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "옷 목록 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      ),
      @ApiResponse(
          responseCode = "404",
          description = "사용자를 찾을 수 없음",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<ClothesCursorResponse> getClothes(
      @Parameter(description = "ownerId", required = true) @RequestParam UUID ownerId,
      @Parameter(description = "cursor") @RequestParam(required = false) UUID cursor,
      @Parameter(description = "idAfter") @RequestParam(required = false) String idAfter,
      @Parameter(description = "limit", example = "10") @RequestParam(defaultValue = "10") int limit, // size → limit
      @Parameter(description = "clothes type filter") @RequestParam(required = false) String typeEqual
  );

  @Operation(
      summary = "옷 등록",
      description = "옷 등록 API"
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "옷 등록 성공",
          content = @Content(schema = @Schema(implementation = ClothesDto.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "옷 등록 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<ClothesDto> saveClothes(
      @Parameter(description = "옷 등록 요청",
          content = @Content(schema = @Schema(implementation = ClothesCreateRequest.class)))
      @RequestPart("request") ClothesCreateRequest request,
      @Parameter(description = "옷 이미지 파일")
      @RequestPart(required = false, value = "image") MultipartFile image
  );

  @Operation(
      summary = "옷 삭제",
      description = "옷 삭제 API"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "옷 삭제 성공"),
      @ApiResponse(
          responseCode = "400",
          description = "옷 삭제 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      ),
      @ApiResponse(
          responseCode = "404",
          description = "옷을 찾을 수 없음",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      ),
      @ApiResponse(
          responseCode = "403",
          description = "삭제 권한 없음",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<Void> delete(
      @Parameter(description = "clothesId") @PathVariable UUID clothesId
  );

  @Operation(
      summary = "옷 수정",
      description = "옷 수정 API"
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "옷 수정 성공",
          content = @Content(schema = @Schema(implementation = ClothesDto.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "옷 수정 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      ),
      @ApiResponse(
          responseCode = "404",
          description = "옷을 찾을 수 없음",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      ),
      @ApiResponse(
          responseCode = "403",
          description = "수정 권한 없음",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<ClothesDto> patchClothes(
      @Parameter(description = "clothesId") @PathVariable UUID clothesId,
      @Parameter(description = "옷 수정 요청",
          content = @Content(schema = @Schema(implementation = ClothesUpdateRequest.class)))
      @RequestPart("request") ClothesUpdateRequest request,
      @Parameter(description = "옷 이미지 파일")
      @RequestPart(required = false, value = "image") MultipartFile image
  );
}
