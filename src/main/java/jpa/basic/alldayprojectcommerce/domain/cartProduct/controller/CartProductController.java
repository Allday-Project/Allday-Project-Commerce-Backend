package jpa.basic.alldayprojectcommerce.domain.cartProduct.controller;

import jakarta.validation.Valid;
import jpa.basic.alldayprojectcommerce.common.ApiResponse;
import jpa.basic.alldayprojectcommerce.common.CursorResponse;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUser;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUserInfo;
import jpa.basic.alldayprojectcommerce.domain.cartProduct.dto.request.CreateCartProductRequest;
import jpa.basic.alldayprojectcommerce.domain.cartProduct.dto.request.UpdateQuantityRequest;
import jpa.basic.alldayprojectcommerce.domain.cartProduct.dto.response.GetAllCartProductResponse;
import jpa.basic.alldayprojectcommerce.domain.cartProduct.service.CartProductCommandService;
import jpa.basic.alldayprojectcommerce.domain.cartProduct.service.CartProductQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartProductController {

    private final CartProductCommandService cartProductCommandService;
    private final CartProductQueryService cartProductQueryService;

    // 장바구니 상품 추가
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createCartProduct(
            @LoginUser LoginUserInfo loginUser,
            @Valid @RequestBody CreateCartProductRequest request) {
        cartProductCommandService.createCartProduct(loginUser.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED));
    }

    // 장바구니 상품 전체 조회
    @GetMapping
    public ResponseEntity<ApiResponse<CursorResponse<GetAllCartProductResponse>>> getAllCartProduct(
            @LoginUser LoginUserInfo loginUser,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int size
    ) {
        CursorResponse<GetAllCartProductResponse> response =
                cartProductQueryService.getAllCartProduct(loginUser.id(), cursorId, size);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, response));
    }

    // 장바구니 상품 수량 변경
    @PatchMapping("/{cartProductId}")
    public ResponseEntity<ApiResponse<Void>> updateQuantity (
            @LoginUser LoginUserInfo loginUser,
            @PathVariable Long cartProductId,
            @Valid @RequestBody UpdateQuantityRequest request) {
        cartProductCommandService.updateQuantity(loginUser.id(), cartProductId, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
    }

}
