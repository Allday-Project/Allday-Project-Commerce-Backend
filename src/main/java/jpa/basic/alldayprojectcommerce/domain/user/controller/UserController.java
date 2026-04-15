package jpa.basic.alldayprojectcommerce.domain.user.controller;

import jakarta.validation.Valid;
import jpa.basic.alldayprojectcommerce.common.ApiResponse;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUser;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUserInfoDto;
import jpa.basic.alldayprojectcommerce.domain.user.dto.request.UpdatemeUserRequest;
import jpa.basic.alldayprojectcommerce.domain.user.dto.response.GetmeUserResponse;
import jpa.basic.alldayprojectcommerce.domain.user.service.UserCommandService;
import jpa.basic.alldayprojectcommerce.domain.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    // 마이페이지 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<GetmeUserResponse>> getMyProfile(@LoginUser LoginUserInfoDto loginUser) {
        GetmeUserResponse response = userQueryService.getProfile(loginUser.id());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, response));
    }

    // 내 정보 수정
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateMyProfile(@LoginUser LoginUserInfoDto loginUser, @RequestBody @Valid UpdatemeUserRequest request) {
        userCommandService.updateProfile(loginUser.id(), request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
    }



}
