package com.part4.team05.sb01otbooteam05.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub", "queue"); // 클라이언트가 구독할 prefix
        registry.setApplicationDestinationPrefixes("/pub"); // 클라이언트가 메시지 보낼 prefix
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // WebSocket 연결 주소
                .setAllowedOriginPatterns(getAllowedOrigins())
                .withSockJS(); // SockJs fallback 지원
    }

    private String[] getAllowedOrigins() {
        return new String[] {
                "http://localhost:3000"
        };
    }
}
