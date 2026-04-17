package jpa.basic.alldayprojectcommerce.domain.cartProduct.service;

import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.cartProduct.dto.request.CreateCartProductRequest;
import jpa.basic.alldayprojectcommerce.domain.cartProduct.dto.request.UpdateQuantityRequest;
import jpa.basic.alldayprojectcommerce.domain.cartProduct.entity.CartProduct;
import jpa.basic.alldayprojectcommerce.domain.cartProduct.repository.CartProductRepository;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import jpa.basic.alldayprojectcommerce.domain.product.entity.ProductStatus;
import jpa.basic.alldayprojectcommerce.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartProductCommandServiceImpl implements CartProductCommandService {

    private final ProductRepository productRepository;
    private final CartProductRepository cartProductRepository;

    // 장바구니 상품 추가
    @Override
    public void createCartProduct(Long userId, CreateCartProductRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 판매 불가 상품 검증
        if (product.getStatus() != ProductStatus.ON_SALE) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_ON_SALE);
        }

        // 재고 검증
        if (product.getStock() < request.quantity()) {
            throw new CustomException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }

        // 이미 담긴 상품이면 수량 합산
        cartProductRepository.findByUserIdAndProductId(userId, request.productId())
                .ifPresentOrElse(
                        existing -> {
                            // 기존수량 + 신규 수량 합산
                            int totalQuantity = existing.getQuantity() + request.quantity();
                            // 합산된 수량이 재고를 초과하는지 체크
                            if (product.getStock() < totalQuantity) {
                                throw new CustomException(ErrorCode.PRODUCT_OUT_OF_STOCK);
                            }
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

    @Override
    public void updateQuantity(
            Long userId, Long cartProductId, UpdateQuantityRequest request) {

        // 장바구니 상품 유무 검증
        CartProduct cartProduct = cartProductRepository.findById(cartProductId)
                .orElseThrow(() -> new CustomException(ErrorCode.CARTPRODUCT_NOT_FOUND));

        // 해당 장바구니 상품에 접근 권한 여부 검증
        if (!cartProduct.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.AUTH_FORBIDDEN_ACCESS);
        }

        // 장바구니에 담긴 상품의 존재 여부 검증
        Product product = productRepository.findById(cartProduct.getProductId())
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 재고 확인
        if (product.getStock() < request.quantity()) {
            throw new CustomException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }

        // @Transactional -> dirty checking으로 자동저장
        cartProduct.updateQuantity(request.quantity());
    }





}
