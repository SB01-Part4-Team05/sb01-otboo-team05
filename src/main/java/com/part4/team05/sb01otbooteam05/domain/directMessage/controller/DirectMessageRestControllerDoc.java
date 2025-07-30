package com.part4.team05.sb01otbooteam05.domain.directMessage.controller;

import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.directMessage.dto.DirectMessageDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "DirectMessage", description = "DirectMessage API")
public interface DirectMessageRestControllerDoc {

  @Operation(
      summary = "DM 목록 조회",
      description = "DM 목록 조회 API",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "DM 목록 조회 성공",
          content = @Content(schema = @Schema(implementation = DirectMessageDtoCursorResponse.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "DM 목록 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      ),
      @ApiResponse(
          responseCode = "401",
          description = "인증 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<DirectMessageDtoCursorResponse> getMessages(
          @Parameter(
                  in = ParameterIn.QUERY,
                  name = "userId",
                  description = "userId",
                  required = true,
                  schema = @Schema(type = "string", format = "uuid")
          )
          @RequestParam("userId") @NotNull UUID userId,

          @Parameter(
                  in = ParameterIn.QUERY,
                  name = "cursor",
                  description = "cursor",
                  required = false,
                  schema = @Schema(type = "string")
          )
          @RequestParam(value = "cursor", required = false) String cursor,

          @Parameter(
                  in = ParameterIn.QUERY,
                  name = "idAfter",
                  description = "idAfter",
                  required = false,
                  schema = @Schema(type = "string", format = "uuid")
          )
          @RequestParam(value = "idAfter", required = false) UUID idAfter,

          @Parameter(
                  in = ParameterIn.QUERY,
                  name = "limit",
                  description = "limit",
                  required = true,
                  schema = @Schema(type = "integer", format = "int32", minimum = "1", maximum = "100"),
                  example = "20"
          )
          @RequestParam("limit") int limit,

          @Parameter(hidden = true)
          CustomUserDetails me
  );
}
