package jpa.basic.alldayprojectcommerce.common.config;

import java.security.Principal;

/**
 * STOMP 전용 Principal 객체
 *
 * HTTP 요총  : JwtAuthenticationFilter  -> SecurityContextHolder
 * WebSocket :  StompChannelInterceptor -> accessor.setUser()
 */
public class StompPrincipal implements Principal {

    private final Long userId;
    private final String role;

    public StompPrincipal(Long userId, String role) {
        this.userId = userId;
        this.role = role;
    }

    @Override
    public String getName() {
        return String.valueOf(userId);
    }

    public Long getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }
}
