package jpa.basic.alldayprojectcommerce.common.security.auth.controller;

import jpa.basic.alldayprojectcommerce.common.security.auth.dto.CreateUserRequest;
import jpa.basic.alldayprojectcommerce.common.security.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/register")
    public String createUser(@RequestBody CreateUserRequest request) {
        return authService.create(request);
    }
}
