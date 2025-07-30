package com.part4.team05.sb01otbooteam05.config;

import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.auth.security.jwt.JwtTokenProvider;
import com.part4.team05.sb01otbooteam05.domain.auth.security.websocket.JwtHandshakeInterceptor;
import com.part4.team05.sb01otbooteam05.domain.user.entity.User;
import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;
import com.part4.team05.sb01otbooteam05.exception.ErrorCode;
import com.part4.team05.sb01otbooteam05.exception.OtbooException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.UUID;

@RequiredArgsConstructor
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    @Value("${websocket.allowed-origins}")
    private String[] allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub", "queue"); // 클라이언트가 구독할 prefix
        registry.setApplicationDestinationPrefixes("/pub"); // 클라이언트가 메시지 보낼 prefix
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOrigins(allowedOrigins)
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String rawToken = accessor.getFirstNativeHeader("Authorization");
                    if (rawToken != null && rawToken.startsWith("Bearer ")) {
                        String token = rawToken.substring(7);
                        if (jwtTokenProvider.validateToken(token)) {
                            UUID userId = jwtTokenProvider.getUserIdFromToken(token);
                            // User 조회 후 UserDetails 생성
                            User user = userRepository.findById(userId)
                                    .orElseThrow(() -> new OtbooException(ErrorCode.USER_NOT_FOUND));
                            CustomUserDetails userDetails = new CustomUserDetails(
                                    user.getId(), user.getEmail(), user.getRole().name()
                            );
                            UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails, null, userDetails.getAuthorities()
                                    );
                            // STOMP 세션의 Principal로 설정
                            accessor.setUser(auth);
                        }
                    }
                }
                return message;
            }
        });
    }
}
