package com.part4.team05.sb01otbooteam05.domain.notification.controller;

import com.part4.team05.sb01otbooteam05.config.SecurityConfig;
import com.part4.team05.sb01otbooteam05.domain.auth.security.jwt.JwtTokenProvider;
import com.part4.team05.sb01otbooteam05.domain.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SseController.class)
@Import(SecurityConfig.class)
class SseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("SSE 연결 성공")
    @WithMockUser
    void subscribe_success() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = "Bearer test.jwt.token";

        given(jwtTokenProvider.getUserIdFromToken("test.jwt.token"))
                .willReturn(userId);
        given(notificationService.connect(eq(userId), isNull()))
                .willReturn(new SseEmitter());

        mockMvc.perform(get("/api/sse")
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("SSE 연결 실패 - 헤더 없음")
    @WithMockUser
    void subscribe_missingHeader() throws Exception {
        mockMvc.perform(get("/api/sse"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("SSE 연결 실패 - 잘못된 Authorization 헤더(prefix)")
    @WithMockUser
    void subscribe_invalidHeaderPrefix() throws Exception {
        mockMvc.perform(get("/api/sse")
                        .header("Authorization", "InvalidFormat"))
                // ↓ 변경: 5xxServerError → BadRequest
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("SSE 연결 실패 - 서비스 에러")
    @WithMockUser
    void subscribe_connectThrowsException() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = "Bearer abc.def.ghi";

        given(jwtTokenProvider.getUserIdFromToken("abc.def.ghi"))
                .willReturn(userId);
        given(notificationService.connect(eq(userId), isNull()))
                .willThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/sse")
                        .header("Authorization", token))
                .andExpect(status().is5xxServerError());
    }
}
