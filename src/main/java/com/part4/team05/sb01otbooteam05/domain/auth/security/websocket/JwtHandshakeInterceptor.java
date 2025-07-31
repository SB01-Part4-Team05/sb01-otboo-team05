package com.part4.team05.sb01otbooteam05.domain.auth.security.websocket;

import com.part4.team05.sb01otbooteam05.domain.auth.security.jwt.JwtTokenProvider;
import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        // 1. HTTP‐based SockJS fallback 요청(skip)
        if (!(request instanceof ServletServerHttpRequest servlet)) {
            return true;
        }
        HttpServletRequest req = servlet.getServletRequest();
        // 실제 WebSocket 업그레이드 요청인지 Sec-WebSocket-Key 헤더로 체크
        if (req.getHeader("Sec-WebSocket-Key") == null) {
            // XHR-polling, info, htmlfile 등 핸드쉐이크가 아닌 요청은 그냥 패스
            return true;
        }

        // 2. 이제 진짜 WebSocket 업그레이드만 검사
        String raw = req.getHeader("Authorization");
        if (raw == null || !raw.startsWith("Bearer ")) {
            log.warn("WebSocket Handshake 실패: Authorization 헤더 없음/형식 오류");
            return false;
        }

        String token = raw.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            log.warn("WebSocket Handshake 실패: 유효하지 않은 토큰");
            return false;
        }

        UUID userId;
        try {
            userId = jwtTokenProvider.getUserIdFromToken(token);
        } catch (Exception e) {
            log.error("WebSocket Handshake 실패: 토큰 파싱 오류", e);
            return false;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("WebSocket Handshake 실패: 사용자 없음 (id={})", userId);
            return false;
        }

        // 3. 성공하면 attributes 에만 담기
        CustomUserDetails userDetails = new CustomUserDetails(
                user.getId(), user.getEmail(), user.getRole().name()
        );
        attributes.put("userDetails", userDetails);

        log.info("WebSocket Handshake 인증 성공: userId={}", userId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, org.springframework.http.server.ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {

    }
}
