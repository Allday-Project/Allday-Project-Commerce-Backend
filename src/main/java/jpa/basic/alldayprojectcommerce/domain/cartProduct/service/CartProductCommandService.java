package jpa.basic.alldayprojectcommerce.domain.cartProduct.service;

import jpa.basic.alldayprojectcommerce.domain.cartProduct.dto.request.CreateCartProductRequest;
import jpa.basic.alldayprojectcommerce.domain.cartProduct.dto.request.UpdateQuantityRequest;

public interface CartProductCommandService {

    // 장바구니 상품 추가(생성)
    void createCartProduct(Long userId, CreateCartProductRequest request);

    // 장바구니 상품 수량 변경
    void updateQuantity(Long userId, Long productId, UpdateQuantityRequest request);
}
