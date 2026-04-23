package jpa.basic.alldayprojectcommerce.common.config;

import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.common.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * STOMP 메시지 전처러 인터셉터
     *
     * CONNECT 시점에만 JWT 검증
     * 이후 SEND / SUBSCRIBE는 이미 인증된 Principal 사용
     *
     * SecurityConfig에서 "/ws-chat/**"를 열어줘야 SockJS 폴백 가능
     * STOMP CONNECT 레벨에서 JWT로 검증
     */
    @Nullable
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        // CONNECT 시점에만 JWT 검증
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractToken(accessor);

            if (token == null || !jwtTokenProvider.validateToken(token)) {
                log.warn("[STOMP] JWT 검증 실패 - 연결 거부");
                throw new CustomException(ErrorCode.CHAT_UNAUTHORIZED);
            }

            Long userId = jwtTokenProvider.getMemberId(token);
            String role = jwtTokenProvider.getRole(token);

            // Principle 설정
            accessor.setUser(new StompPrincipal(userId, role));
            log.info("[STOMP] 연결 성공 userId: {}, role: {}", userId, role);
        }

        return message;
    }

    // Authorization 헤더에서 Bearer 토큰 추출
    private String extractToken(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
