package jpa.basic.alldayprojectcommerce.domain.cartProduct.controller;

import jakarta.validation.Valid;
import jpa.basic.alldayprojectcommerce.common.ApiResponse;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUser;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUserInfo;
import jpa.basic.alldayprojectcommerce.domain.cartProduct.dto.request.CreateCartProductRequest;
import jpa.basic.alldayprojectcommerce.domain.cartProduct.service.CartProductCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartProductController {

    private final CartProductCommandService cartProductCommandService;

    // 장바구니 상품 추가
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createCartProduct(
            @LoginUser LoginUserInfo loginUser,
            @Valid @RequestBody CreateCartProductRequest request) {
        cartProductCommandService.createCartProduct(loginUser.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED));
    }

}
