package jpa.basic.alldayprojectcommerce.domain.user.controller;

import jpa.basic.alldayprojectcommerce.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

}
