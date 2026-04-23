package jpa.basic.alldayprojectcommerce.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompChannelInterceptor stompChannelInterceptor;

    @Value("${websocket.allowed-origins}")
    private String allowedOrigins;

    /**
     * 메시지 브로커 설정
     *
     * /sub -> 구독 prefix (서버 -> 클라이언트)
     *
     * /pub -> 발행 prefix (클라이언트 -> 서버)
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");    // 구독 경로  prefix
        registry.setApplicationDestinationPrefixes("/pub");      // 발행 경로  prefix
    }

    /**
     * STOMP 엔드포인트 등록
     *
     * 클라이언트 WebSocket 연결 시 접속하는 URL
     * "ws://localhost:8090/ws-chat"
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns(allowedOrigins.split(","))      // 운영할 때는 도메인 지정
                .withSockJS();           // WebSocket 미지원 브라우저 fallback 처리
    }

    /**
     * ChannelInterceptor 등록
     * STOMP CONNECT 시점에 JWT 검증 수행
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompChannelInterceptor);
    }
}
