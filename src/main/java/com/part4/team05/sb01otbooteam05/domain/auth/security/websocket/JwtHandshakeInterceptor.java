package com.part4.team05.sb01otbooteam05.domain.auth.security.websocket;

import com.part4.team05.sb01otbooteam05.domain.auth.security.jwt.JwtTokenProvider;
import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, org.springframework.http.server.ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String token = httpRequest.getHeader("Authorization");

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                if (jwtTokenProvider.validateToken(token)) {
                    UUID userId = jwtTokenProvider.getUserIdFromToken(token);
                    Optional<User> userOpt = userRepository.findById(userId);

                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        // CustomUserDetails 생성자 호출부 수정
                        CustomUserDetails userDetails = new CustomUserDetails(
                                user.getId(),
                                user.getEmail(),
                                user.getRole().name()  // role 필드가 String 타입이므로 .name() 또는 직접 String 전달
                        );
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.info("WebSocket 인증 완료: userId={}", user.getId());
                    }
                }
            }
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, org.springframework.http.server.ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {

    }
}
