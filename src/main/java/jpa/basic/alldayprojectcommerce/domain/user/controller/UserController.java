package jpa.basic.alldayprojectcommerce.domain.user.controller;

import jakarta.validation.Valid;
import jpa.basic.alldayprojectcommerce.common.ApiResponse;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUser;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUserInfo;
import jpa.basic.alldayprojectcommerce.domain.user.dto.request.UpdatePasswordRequest;
import jpa.basic.alldayprojectcommerce.domain.user.dto.request.UpdatemeUserRequest;
import jpa.basic.alldayprojectcommerce.domain.user.dto.response.GetUnmaskedUserResponse;
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
    public ResponseEntity<ApiResponse<GetmeUserResponse>> getMyProfile(
            @LoginUser LoginUserInfo loginUser) {
        GetmeUserResponse response = userQueryService.getProfile(loginUser.id());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, response));
    }

    // 내 정보 조회 (마스킹 없음) - 주문서 작성 등
    @GetMapping("/me/unmasked")
    public ResponseEntity<ApiResponse<GetUnmaskedUserResponse>> getMyProfileUnmasked(
            @LoginUser LoginUserInfo loginUser) {
        GetUnmaskedUserResponse response = userQueryService.getUnmaskedProfile(loginUser.id());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, response));
    }

    // 내 정보 수정
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateMyProfile(
            @LoginUser LoginUserInfo loginUser,
            @RequestBody @Valid UpdatemeUserRequest request) {
        userCommandService.updateProfile(loginUser.id(), request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
    }

    // 비밀번호 수정
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @LoginUser LoginUserInfo loginUser,
            @RequestBody @Valid UpdatePasswordRequest request) {
        userCommandService.updatePassword(loginUser.id(), request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
    }
}
