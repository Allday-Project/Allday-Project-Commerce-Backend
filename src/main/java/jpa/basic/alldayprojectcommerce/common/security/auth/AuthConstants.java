package jpa.basic.alldayprojectcommerce.common.security.auth;

public class AuthConstants {
    public static final String AUTHORIZATION_HEADER = "Authorization";  // 요청 헤더 키 이름
    public static final String BEARER_PREFIX        = "Bearer ";        // 토큰 앞에 붙는 접두사 (공백 포함)
    public static final String REFRESH_TOKEN        = "refresh_token";  // 쿠키 이름
    public static final String COOKIE_PATH_ROOT     = "/";              // 쿠키 유효 경로
}