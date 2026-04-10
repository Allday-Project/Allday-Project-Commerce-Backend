package jpa.basic.alldayprojectcommerce.common.security.auth;

public record AuthTokens(
        String accessToken,
        String refreshToken
) {}