package com.part4.team05.sb01otbooteam05.domain.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team05.sb01otbooteam05.config.SecurityConfig;
import com.part4.team05.sb01otbooteam05.domain.auth.security.jwt.JwtTokenProvider;
import com.part4.team05.sb01otbooteam05.domain.notification.dto.NotificationDtoCursorResponse;
import com.part4.team05.sb01otbooteam05.domain.notification.service.NotificationService;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.service.UserService;
import com.part4.team05.sb01otbooteam05.exception.GlobalExceptionHandler;
import com.part4.team05.sb01otbooteam05.exception.ErrorCode;
import com.part4.team05.sb01otbooteam05.exception.OtbooException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private UserService userService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private static final String TOKEN = "Bearer valid-token";
    private UUID userId;
    private User mockUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockUser = mock(User.class);
        given(mockUser.getId()).willReturn(userId);
        given(jwtTokenProvider.getUserIdFromToken(anyString())).willReturn(userId);
        given(userService.getUserEntityByIdOrThrow(eq(userId))).willReturn(mockUser);
    }

    // --- GET /api/notifications ---

    @Test
    @DisplayName("알림 목록 조회 성공 - 기본 파라미터")
    @WithMockUser
    void getNotifications_defaultParams_success() throws Exception {
        given(notificationService.getNotifications(eq(mockUser), isNull(), eq(5)))
                .willReturn(new NotificationDtoCursorResponse(
                        Collections.emptyList(), null, null, false, 0, "createdAt", "DESCENDING"
                ));

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("알림 목록 조회 성공 - idAfter, limit 지정")
    @WithMockUser
    void getNotifications_success() throws Exception {
        UUID idAfter = UUID.randomUUID();
        given(notificationService.getNotifications(eq(mockUser), eq(idAfter), eq(5)))
                .willReturn(new NotificationDtoCursorResponse(
                        Collections.emptyList(), null, idAfter, false, 0, "createdAt", "DESCENDING"
                ));

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", TOKEN)
                        .param("idAfter", idAfter.toString())
                        .param("limit", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextIdAfter").value(idAfter.toString()));
    }

    @Test
    @DisplayName("알림 목록 조회 성공 - limit만 지정")
    @WithMockUser
    void getNotifications_limitOnly_success() throws Exception {
        int customLimit = 10;
        given(notificationService.getNotifications(eq(mockUser), isNull(), eq(customLimit)))
                .willReturn(new NotificationDtoCursorResponse(
                        Collections.emptyList(), null, null, false, customLimit, "createdAt", "DESCENDING"
                ));

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", TOKEN)
                        .param("limit", String.valueOf(customLimit))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("알림 목록 조회 성공 - 토큰 정상 파싱 분기 강제 커버")
    @WithMockUser
    void getNotifications_fullBranchCoverage_force() throws Exception {
        // 이 토큰은 "Bearer " 접두어 O, 나머지 값도 존재하여 전체 조건 false
        String fullValidToken = "Bearer some-valid-token";
        UUID mockId = UUID.randomUUID();

        given(jwtTokenProvider.getUserIdFromToken("some-valid-token"))
                .willReturn(mockId);

        User dummyUser = mock(User.class);
        given(dummyUser.getId()).willReturn(mockId);
        given(userService.getUserEntityByIdOrThrow(mockId)).willReturn(dummyUser);
        given(notificationService.getNotifications(eq(dummyUser), isNull(), eq(5)))
                .willReturn(new NotificationDtoCursorResponse(
                        Collections.emptyList(), null, null, false, 5, "createdAt", "DESCENDING"
                ));

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", fullValidToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("알림 목록 조회 실패 - 토큰 없음")
    @WithMockUser
    void getNotifications_missingHeader() throws Exception {
        mockMvc.perform(get("/api/notifications")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("알림 목록 조회 실패 - 토큰 타입 오류 (prefix)")
    @WithMockUser
    void getNotifications_invalidHeaderPrefix() throws Exception {
        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "InvalidFormat")
                        .param("limit", "5")
                        .accept(MediaType.APPLICATION_JSON))
                // OtbooException from prefix error is handled as 5xx
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("알림 목록 조회 실패 - 토큰 타입 오류 (provider)")
    @WithMockUser
    void getNotifications_invalidHeaderFormat() throws Exception {
        // stub jwt provider to throw for any token
        given(jwtTokenProvider.getUserIdFromToken(anyString()))
                .willThrow(new OtbooException(ErrorCode.UNAUTHORIZED));

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer invalid-token")
                        .param("limit", "5"))
                // OtbooException from provider error is handled as 5xx
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("알림 목록 조회 실패 - UUID 포맷 오류")
    @WithMockUser
    void getNotifications_badRequest_uuid() throws Exception {
        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", TOKEN)
                        .param("idAfter", "invalid-uuid")
                        .param("limit", "5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("알림 목록 조회 실패 - limit 범위 벗어남")
    @WithMockUser
    void getNotifications_badRequest_limit() throws Exception {
        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", TOKEN)
                        .param("limit", "0"))
                // ConstraintViolationException is handled as 5xx
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("알림 목록 조회 실패 - 권한 없음")
    @WithMockUser
    void getNotifications_forbidden() throws Exception {
        given(notificationService.getNotifications(eq(mockUser), any(), anyInt()))
                .willThrow(new OtbooException(ErrorCode.NOTIFICATION_UNAUTHORIZED));

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", TOKEN)
                        .param("limit", "5"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("알림 목록 조회 실패 - Bearer 접두어만 있고 토큰 없음")
    @WithMockUser
    void getNotifications_emptyToken_afterBearer() throws Exception {
        given(jwtTokenProvider.getUserIdFromToken(""))
                .willThrow(new OtbooException(ErrorCode.UNAUTHORIZED));

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer ") // 공백 토큰
                        .param("limit", "5"))
                .andExpect(status().is5xxServerError());
    }


    // --- DELETE /api/notifications/{id} ---

    @Test
    @DisplayName("알림 읽음 처리 성공")
    @WithMockUser
    void markAsRead_success() throws Exception {
        UUID notifId = UUID.randomUUID();
        doNothing().when(notificationService).markAsRead(eq(notifId), eq(userId));

        mockMvc.perform(delete("/api/notifications/{id}", notifId)
                        .header("Authorization", TOKEN)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("알림 읽음 처리 실패 - 토큰 없음")
    @WithMockUser
    void markAsRead_missingHeader() throws Exception {
        UUID notifId = UUID.randomUUID();
        mockMvc.perform(delete("/api/notifications/{id}", notifId)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("알림 읽음 처리 실패 - 토큰 타입 오류 (prefix)")
    @WithMockUser
    void markAsRead_invalidHeaderPrefix() throws Exception {
        UUID notifId = UUID.randomUUID();
        mockMvc.perform(delete("/api/notifications/{id}", notifId)
                        .header("Authorization", "InvalidFormat")
                        .with(csrf()))
                // OtbooException from prefix error is handled as 5xx
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("알림 읽음 처리 실패 - 토큰 타입 오류 (provider)")
    @WithMockUser
    void markAsRead_invalidHeaderFormat() throws Exception {
        UUID notifId = UUID.randomUUID();
        given(jwtTokenProvider.getUserIdFromToken(anyString()))
                .willThrow(new OtbooException(ErrorCode.UNAUTHORIZED));

        mockMvc.perform(delete("/api/notifications/{id}", notifId)
                        .header("Authorization", "Bearer bad-token")
                        .with(csrf()))
                // OtbooException from provider error is handled as 5xx
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("알림 읽음 처리 실패 - UUID 포맷 오류")
    @WithMockUser
    void markAsRead_badRequest_uuid() throws Exception {
        mockMvc.perform(delete("/api/notifications/{id}", "invalid-uuid")
                        .header("Authorization", TOKEN)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("알림 읽음 처리 실패 - 알림 없음")
    @WithMockUser
    void markAsRead_notFound() throws Exception {
        UUID notifId = UUID.randomUUID();
        doThrow(new OtbooException(ErrorCode.NOTIFICATION_NOT_FOUND))
                .when(notificationService).markAsRead(eq(notifId), eq(userId));

        mockMvc.perform(delete("/api/notifications/{id}", notifId)
                        .header("Authorization", TOKEN)
                        .with(csrf()))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("알림 읽음 처리 실패 - 권한 없음")
    @WithMockUser
    void markAsRead_forbidden() throws Exception {
        UUID notifId = UUID.randomUUID();
        doThrow(new OtbooException(ErrorCode.NOTIFICATION_UNAUTHORIZED))
                .when(notificationService).markAsRead(eq(notifId), eq(userId));

        mockMvc.perform(delete("/api/notifications/{id}", notifId)
                        .header("Authorization", TOKEN)
                        .with(csrf()))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("알림 삭제 실패 - CSRF 토큰 없음")
    @WithMockUser
    void deleteNotification_missingCsrf() throws Exception {
        UUID notifId = UUID.randomUUID();
        mockMvc.perform(delete("/api/notifications/{id}", notifId)
                        .header("Authorization", TOKEN))
                .andExpect(status().isForbidden());
    }
}
