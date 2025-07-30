package com.part4.team05.sb01otbooteam05.domain.feed.controller;

import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDto;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.FeedDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FeedCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.dto.request.FeedUpdateRequest;
import com.part4.team05.sb01otbooteam05.domain.feed.enums.SortType;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDto;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.CommentDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.feedComment.dto.request.CommentCreateRequest;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.PrecipitationType;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.SkyStatusType;
import com.part4.team05.sb01otbooteam05.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.UUID;

@Tag(name = "피드 관리", description = "피드 관련 API")
public interface FeedControllerDoc {

  @Operation(
      summary = "피드 목록 조회",
      description = "피드 목록 조회 API",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "피드 목록 조회 성공",
          content = @Content(schema = @Schema(implementation = FeedDtoCursorResponse.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "피드 목록 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<FeedDtoCursorResponse> findFeeds(
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user,
      @Parameter(description = "cursor") @RequestParam(value = "cursor", defaultValue = "", required = false) String cursor,
      @Parameter(description = "idAfter") @RequestParam(value = "idAfter", required = false) UUID idAfter,
      @Parameter(description = "limit") @RequestParam(value = "limit", defaultValue = "20", required = true) Integer limit,
      @Parameter(description = "sortBy") @RequestParam(value = "sortBy", required = false) SortType sortBy,
      @Parameter(description = "sortDirection") @RequestParam(value = "sortDirection", defaultValue = "DESCENDING", required = true) String sortDirection,
      @Parameter(description = "keywordLike") @RequestParam(value = "keywordLike", defaultValue = "", required = false) String keywordLike,
      @Parameter(description = "skyStatusEqual") @RequestParam(value = "skyStatusEqual", required = false) SkyStatusType skyStatusEqual,
      @Parameter(description = "precipitationTypeEqual") @RequestParam(value = "precipitationTypeEqual", required = false) PrecipitationType precipitationTypeEqual,
      @Parameter(description = "authorIdEqual") @RequestParam(value = "authorIdEqual", required = false) UUID authorIdEqual
  );

  @Operation(
      summary = "피드 등록",
      description = "피드 등록 API",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "피드 등록 성공",
          content = @Content(schema = @Schema(implementation = FeedDto.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "피드 등록 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<FeedDto> createFeed(
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user,
      @Parameter(description = "피드 등록 요청") @Validated @RequestBody FeedCreateRequest request
  );

  @Operation(
      summary = "피드 좋아요",
      description = "피드 좋아요 API",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "피드 좋아요 성공",
          content = @Content(schema = @Schema(implementation = FeedDto.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "피드 좋아요 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<FeedDto> likeFeed(
      @Parameter(description = "feedId") @PathVariable("feedId") UUID feedId,
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user
  );

  @Operation(
      summary = "피드 좋아요 취소",
      description = "피드 좋아요 취소 API",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "피드 좋아요 취소 성공"),
      @ApiResponse(
          responseCode = "400",
          description = "피드 좋아요 취소 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<FeedDto> unlikeFeed(
      @Parameter(description = "feedId") @PathVariable("feedId") UUID feedId,
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user
  );

  @Operation(
      summary = "피드 댓글 조회",
      description = "피드 댓글 조회 API",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "피드 댓글 조회 성공",
          content = @Content(schema = @Schema(implementation = FeedDto.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "피드 댓글 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<CommentDtoCursorResponse> findFeedComments(
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user,
      @Parameter(description = "feedId") @RequestParam(value = "feedId", required = true) UUID feedId,
      @Parameter(description = "cursor") @RequestParam(value = "cursor", defaultValue = "") LocalDateTime cursor,
      @Parameter(description = "idAfter") @RequestParam(value = "idAfter", defaultValue = "") UUID idAfter,
      @Parameter(description = "limit") @RequestParam(value = "limit") Integer limit
  );

  @Operation(
      summary = "피드 댓글 등록",
      description = "피드 댓글 등록 API",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "피드 댓글 등록 성공",
          content = @Content(schema = @Schema(implementation = CommentDto.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "피드 댓글 등록 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<CommentDto> createFeedComment(
      @Parameter(description = "feedId") @PathVariable("feedId") UUID feedId,
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user,
      @Parameter(description = "피드 댓글 등록 요청") @Validated @RequestBody CommentCreateRequest request
  );

  @Operation(
      summary = "피드 삭제",
      description = "피드 삭제 API",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "피드 삭제 성공"),
      @ApiResponse(
          responseCode = "400",
          description = "피드 삭제 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<Void> deleteFeed(
      @Parameter(description = "feedId") @PathVariable("feedId") UUID feedId,
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user
  );

  @Operation(
      summary = "피드 수정",
      description = "피드 수정 API",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "피드 수정 성공",
          content = @Content(schema = @Schema(implementation = FeedDto.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "피드 수정 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<FeedDto> updateFeed(
      @Parameter(description = "feedId") @PathVariable("feedId") UUID feedId,
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user,
      @Parameter(description = "피드 수정 요청") @Validated @RequestBody FeedUpdateRequest request
  );
}
