package com.part4.team05.sb01otbooteam05.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@Tag(name = "sse-controller", description = "")
public interface SseControllerDoc {

  @Operation(
      summary = "",
      description = "",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "OK",
          content = @Content(mediaType = "text/event-stream")
      )
  })
  SseEmitter subscribe(
      @Parameter(description = "Authorization")
      @RequestHeader("Authorization") String authorizationHeader,

      @Parameter(description = "LastEventId")
      @RequestParam(required = false) UUID lastEventId
  );
}
