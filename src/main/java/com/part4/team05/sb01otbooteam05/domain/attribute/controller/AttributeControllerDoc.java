package com.part4.team05.sb01otbooteam05.domain.attribute.controller;

import com.part4.team05.sb01otbooteam05.domain.attribute.dto.ClothesAttributeDefDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeDefinition;
import com.part4.team05.sb01otbooteam05.domain.attribute.dto.ClothesAttributeDefCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.attribute.dto.ClothesAttributeDefUpdateRequest;
import com.part4.team05.sb01otbooteam05.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "의상 속성 정의", description = "의상 속성 정의 관련 API")
public interface AttributeControllerDoc {

  @Operation(
      summary = "의상 속성 정의 등록",
      description = "의상 속성 정의 등록 API",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "의상 속성 정의 등록 성공",
          content = @Content(schema = @Schema(implementation = AttributeDefinition.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "의상 속성 정의 등록 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<AttributeDefinition> createDef(
      @Parameter(description = "의상 속성 정의 등록 요청") @RequestBody ClothesAttributeDefCreateRequest request
  );

  @Operation(
      summary = "의상 속성 정의 수정",
      description = "의상 속성 정의 수정 API",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "의상 속성 정의 수정 성공",
          content = @Content(schema = @Schema(implementation = AttributeDefinition.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "의상 속성 정의 수정 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<AttributeDefinition> update(
      @Parameter(description = "definitionId") @PathVariable UUID definitionId,
      @Parameter(description = "의상 속성 정의 수정 요청") @RequestBody ClothesAttributeDefUpdateRequest request
  );

  @Operation(
      summary = "의상 속성 정의 삭제",
      description = "의상 속성 정의 삭제 API",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "의상 속성 정의 삭제 성공"),
      @ApiResponse(
          responseCode = "400",
          description = "의상 속성 정의 삭제 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<Void> deleteDef(
      @Parameter(description = "definitionId") @PathVariable UUID definitionId
  );

  @Operation(
      summary = "의상 속성 정의 목록 조회",
      description = "의상 속성 정의 목록 조회 API"
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "의상 속성 정의 목록 조회 성공",
          content = @Content(schema = @Schema(implementation = ClothesAttributeDefDtoCursorResponse.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "의상 속성 정의 목록 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<ClothesAttributeDefDtoCursorResponse> getAttributes(
      @Parameter(description = "cursor") @RequestParam(required = false) UUID cursor,
      @Parameter(description = "size", example = "10") @RequestParam(defaultValue = "10") int size
  );
}
