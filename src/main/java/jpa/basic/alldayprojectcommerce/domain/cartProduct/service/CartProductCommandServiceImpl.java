package jpa.basic.alldayprojectcommerce.domain.cartProduct.service;

import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.cartProduct.dto.request.CreateCartProductRequest;
import jpa.basic.alldayprojectcommerce.domain.cartProduct.dto.request.UpdateQuantityRequest;
import jpa.basic.alldayprojectcommerce.domain.cartProduct.entity.CartProduct;
import jpa.basic.alldayprojectcommerce.domain.cartProduct.repository.CartProductRepository;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import jpa.basic.alldayprojectcommerce.domain.product.entity.ProductStatus;
import jpa.basic.alldayprojectcommerce.domain.product.service.ProductQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class CartProductCommandServiceImpl implements CartProductCommandService {

    private final ProductQueryService productQueryService;
    private final CartProductRepository cartProductRepository;

    // 장바구니 상품 추가
    @Override
    public void createCartProduct(Long userId, CreateCartProductRequest request) {
        // 상품 존재 여부 검증
        Product product = productQueryService.getByProductId(request.productId());

        // 판매 상태 검증
        if (product.getStatus() != ProductStatus.ON_SALE) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_ON_SALE);
        }

        // 이미 담긴 상품이면 수량 합산
        cartProductRepository.findByUserIdAndProductId(userId, request.productId())
                .ifPresentOrElse(
                        existing -> {
                            // 기존수량 + 신규 수량 합산
                            int totalQuantity = existing.getQuantity() + request.quantity();
                            existing.updateQuantity(totalQuantity);
                        },
                        () -> cartProductRepository.save(
                                CartProduct.builder()
                                        .userId(userId)
                                        .productId(request.productId())
                                        .quantity(request.quantity())
                                        .build()
                        )
                );
    }

    // 수량 변경
    @Override
    public void updateQuantity(Long userId, Long cartProductId, UpdateQuantityRequest request) {

        // 장바구니 상품 존재 여부 검증
        CartProduct cartProduct = getCartProductOrThrow(cartProductId);

        // 해당 장바구니 접근 권한 검증
        validateOwner(cartProduct, userId);

        // @Transactional -> dirty checking으로 자동저장
        cartProduct.updateQuantity(request.quantity());
    }

    // 장바구니 상품 단건 삭제
    @Override
    public void deleteCartProduct(Long userId, Long cartProductId) {

        // 장바구니 상품 존재 여부 검증
        CartProduct cartProduct = getCartProductOrThrow(cartProductId);

        // 해당 장바구니 접근 권한 검증
        validateOwner(cartProduct, userId);

        // 해당 장바구니 상품 삭제 (hard delete)
        cartProductRepository.delete(cartProduct);
    }

    // 장바구니 비우기
    @Override
    public void cleanCart(Long userId) {
        cartProductRepository.deleteAllByUserId(userId);
    }


    // ======= 장바구니 공통 검증 로직 ========

    // 장바구니에 상품 존재 여부 검증 로직
    private CartProduct getCartProductOrThrow(Long cartProductId) {
        return cartProductRepository.findById(cartProductId)
                .orElseThrow(() -> new CustomException(ErrorCode.CARTPRODUCT_NOT_FOUND));
    }

    // 해당 장바구니 상품에 접근 권한 여부 검증
    private void validateOwner(CartProduct cartProduct, Long userId) {
        if (!cartProduct.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.AUTH_FORBIDDEN_ACCESS);
        }
    }
}
