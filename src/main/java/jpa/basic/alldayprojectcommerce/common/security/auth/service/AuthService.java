package jpa.basic.alldayprojectcommerce.common.security.auth.service;

import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.common.security.auth.dto.CreateUserRequest;
import jpa.basic.alldayprojectcommerce.common.security.jwt.JwtTokenProvider;
import jpa.basic.alldayprojectcommerce.domain.user.exception.DuplicateEmailException;
import jpa.basic.alldayprojectcommerce.domain.user.service.UserQueryService;
import jpa.basic.alldayprojectcommerce.domain.user.service.UserCommandServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserQueryService userQueryService;
    private final UserCommandServiceImpl userCommandServiceImpl;

    public String create(CreateUserRequest request) {
        boolean isExists = userQueryService.getUserByEmail(request.email());

        if (isExists) {
            throw new DuplicateEmailException(ErrorCode.DUPLICATE_EMAIL);
        }

        // TODO: userService를 통한 사용자 생성 로직 구현

        return jwtTokenProvider.createAccessToken(0L, "");
    }
}
