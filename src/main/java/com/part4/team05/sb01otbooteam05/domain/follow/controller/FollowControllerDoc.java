package com.part4.team05.sb01otbooteam05.domain.follow.controller;

import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowDto;
import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowListResponse;
import com.part4.team05.sb01otbooteam05.domain.follow.dto.FollowSummaryDto;
import com.part4.team05.sb01otbooteam05.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "팔로우 관리", description = "팔로우 관련 API")
public interface FollowControllerDoc {

  @Operation(
      summary = "팔로우 생성",
      description = "팔로우 생성 API"
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "팔로우 생성 성공",
          content = @Content(schema = @Schema(implementation = FollowDto.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "팔로우 생성 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<FollowDto> createFollow(
      @Parameter(description = "팔로우 생성 요청") @RequestBody @Valid FollowCreateRequest request
  );

  @Operation(
      summary = "팔로우 요약 정보 조회",
      description = "팔로우 요약 정보 조회 API"
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "팔로우 요약 정보 조회 성공",
          content = @Content(schema = @Schema(implementation = FollowSummaryDto.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "팔로우 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<FollowSummaryDto> getFollowSummary(
      @Parameter(description = "userId") @RequestParam UUID userId,
      @Parameter(description = "currentUserId") @RequestParam UUID currentUserId
  );

  @Operation(
      summary = "팔로잉 목록 조회",
      description = "팔로잉 목록 조회 API"
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "팔로잉 목록 조회 성공",
          content = @Content(schema = @Schema(implementation = FollowListResponse.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "팔로잉 목록 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<FollowListResponse> getFollowings(
      @Parameter(description = "followerId") @RequestParam @NotNull UUID followerId,
      @Parameter(description = "cursor") @RequestParam(required = false) String cursor,
      @Parameter(description = "idAfter") @RequestParam(required = false) UUID idAfter,
      @Parameter(description = "limit") @RequestParam @Min(1) int limit,
      @Parameter(description = "nameLike") @RequestParam(required = false) String nameLike
  );

  @Operation(
      summary = "팔로워 목록 조회",
      description = "팔로워 목록 조회 API"
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "팔로워 목록 조회 성공",
          content = @Content(schema = @Schema(implementation = FollowListResponse.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "팔로워 목록 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<FollowListResponse> getFollowers(
      @Parameter(description = "followeeId") @RequestParam @NotNull UUID followeeId,
      @Parameter(description = "cursor") @RequestParam(required = false) String cursor,
      @Parameter(description = "idAfter") @RequestParam(required = false) UUID idAfter,
      @Parameter(description = "limit") @RequestParam @Min(1) int limit,
      @Parameter(description = "nameLike") @RequestParam(required = false) String nameLike
  );

  @Operation(
      summary = "팔로우 취소",
      description = "팔로우 취소 API",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "팔로우 취소 성공"),
      @ApiResponse(
          responseCode = "400",
          description = "팔로우 취소 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<Void> unfollow(
      @Parameter(description = "followId") @PathVariable UUID followId,
      @Parameter(description = "currentUserId") @RequestHeader("X-USER-ID") UUID currentUserId
  );
}
