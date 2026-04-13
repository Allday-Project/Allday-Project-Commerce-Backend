package jpa.basic.alldayprojectcommerce.common.security.auth.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.common.security.auth.AuthConstants;
import jpa.basic.alldayprojectcommerce.common.security.auth.AuthTokens;
import jpa.basic.alldayprojectcommerce.common.security.auth.dto.request.CreateUserRequest;
import jpa.basic.alldayprojectcommerce.common.security.auth.dto.request.LoginUserRequest;
import jpa.basic.alldayprojectcommerce.common.security.auth.exception.AuthUnauthenticatedException;
import jpa.basic.alldayprojectcommerce.common.security.cookie.CookieUtils;
import jpa.basic.alldayprojectcommerce.common.security.jwt.JwtTokenProvider;
import jpa.basic.alldayprojectcommerce.domain.user.entity.User;
import jpa.basic.alldayprojectcommerce.domain.user.service.UserCommandService;
import jpa.basic.alldayprojectcommerce.domain.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder       passwordEncoder;
    private final JwtTokenProvider      jwtTokenProvider;
    private final UserQueryService      userQueryService;
    private final UserCommandService    userCommandService;
    private final CookieUtils           cookieUtils;

    public void signup(CreateUserRequest request, HttpServletResponse response) {
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = userCommandService.create(request.email(), encodedPassword);

        AuthTokens authTokens = AuthTokens.of(
                jwtTokenProvider.createAccessToken(user.getId(), user.getRole().name()),
                jwtTokenProvider.createRefreshToken(user.getId())
        );

        response.addCookie(cookieUtils.createCookie(
                AuthConstants.ACCESS_TOKEN, authTokens.accessToken(), 30 * 60
        ));
        response.addCookie(cookieUtils.createCookie(
                AuthConstants.REFRESH_TOKEN, authTokens.refreshToken(), jwtTokenProvider.getRefreshTokenValidityInSeconds()
        ));
    }

    public void login(LoginUserRequest request, HttpServletResponse response) {
        User user = userQueryService.getByEmail(request.email());

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthUnauthenticatedException(ErrorCode.AUTH_UNAUTHENTICATED_USER);
        }

        AuthTokens authTokens = AuthTokens.of(
                jwtTokenProvider.createAccessToken(user.getId(), user.getRole().name()),
                jwtTokenProvider.createRefreshToken(user.getId())
        );

        response.addCookie(cookieUtils.createCookie(
                AuthConstants.ACCESS_TOKEN,
                authTokens.accessToken(),
                30 * 60                                                 // 30분
        ));

        response.addCookie(cookieUtils.createCookie(
                AuthConstants.REFRESH_TOKEN,
                authTokens.refreshToken(),
                jwtTokenProvider.getRefreshTokenValidityInSeconds()     // 7일
        ));
    }

    public void logout(HttpServletResponse response) {
        response.addCookie(cookieUtils.deleteCookie(AuthConstants.ACCESS_TOKEN));
        response.addCookie(cookieUtils.deleteCookie(AuthConstants.REFRESH_TOKEN));
    }

    public void reissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);

        if (refreshToken == null) {
            throw new AuthUnauthenticatedException(ErrorCode.AUTH_UNAUTHENTICATED_USER);
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            response.addCookie(cookieUtils.deleteCookie(AuthConstants.ACCESS_TOKEN));
            response.addCookie(cookieUtils.deleteCookie(AuthConstants.REFRESH_TOKEN));
            throw new AuthUnauthenticatedException(ErrorCode.AUTH_UNAUTHENTICATED_USER);
        }

        Long userId = jwtTokenProvider.getMemberId(refreshToken);
        User user = userQueryService.getById(userId);

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole().name());
        response.addCookie(cookieUtils.createCookie(
                AuthConstants.ACCESS_TOKEN,
                accessToken,
                30 * 60
        ));
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        return Arrays.stream(cookies)
                .filter(cookie -> AuthConstants.REFRESH_TOKEN.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
