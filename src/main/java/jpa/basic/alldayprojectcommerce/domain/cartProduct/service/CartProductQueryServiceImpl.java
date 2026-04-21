package jpa.basic.alldayprojectcommerce.domain.cartProduct.service;

import jpa.basic.alldayprojectcommerce.common.CursorResponse;
import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.cartProduct.dto.response.GetAllCartProductResponse;
import jpa.basic.alldayprojectcommerce.domain.cartProduct.entity.CartProduct;
import jpa.basic.alldayprojectcommerce.domain.cartProduct.repository.CartProductRepository;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import jpa.basic.alldayprojectcommerce.domain.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
import jpa.basic.alldayprojectcommerce.common.CursorResponse;
import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.cartProduct.dto.response.GetAllCartProductResponse;
import jpa.basic.alldayprojectcommerce.domain.cartProduct.entity.CartProduct;
import jpa.basic.alldayprojectcommerce.domain.cartProduct.repository.CartProductRepository;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import jpa.basic.alldayprojectcommerce.domain.product.service.ProductQueryService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartProductQueryServiceImpl implements CartProductQueryService{

    private final CartProductRepository cartProductRepository;
    private final ProductQueryService productQueryService;

    @Override
    public CursorResponse<GetAllCartProductResponse> getAllCartProduct(
            Long loginId, Long cursorId, int size) {

        long cursor = (cursorId == null) ? Long.MAX_VALUE : cursorId;

        // 1. 데이터 조회
        List<CartProduct> cartProducts = cartProductRepository
                .findAllByUserIdWithCursor(loginId, cursor, size);

        // 2. 조회된 장바구니에 있는 모든 상품 id 추출
        List<Long> productIds = cartProducts.stream()
                .map(CartProduct::getProductId)
                .toList();

        // 3. ProductQueryService를 통해 상품 정보 한번에 조회
        List<Product> products = productQueryService.findAllByIds(productIds);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(
                        Product::getId,
                        p -> p,
                        (existing, replacement) -> existing
                ));

        // 4. DTO 변환
        List<GetAllCartProductResponse> responseList = cartProducts.stream()
                .map(cartProduct -> {
                    // 상품 정보를 레포지토리에서 조회 (혹은 fetch join 사용권장)
                    Product product = productMap.get(cartProduct.getProductId());
                    if(product == null) {
                        throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
                    }
                    return GetAllCartProductResponse.from(cartProduct, product);
                }).toList();

        // 3. 응답 생성
        return CursorResponse.of(
                responseList,
                size,
                GetAllCartProductResponse::cartProductId
        );
    }

    private final CartProductRepository cartProductRepository;
    private final ProductRepository productRepository;

    @Override
    public CursorResponse<GetAllCartProductResponse> getAllCartProduct(
            Long loginId, Long cursorId, int size) {

        long cursor = (cursorId == null) ? Long.MAX_VALUE : cursorId;

        // 1. 데이터 조회
        List<CartProduct> cartProducts = cartProductRepository.findAllByUserIdWithCursor(loginId, cursor, size);

        // 2. DTO 변환
        List<GetAllCartProductResponse> responsesList = cartProducts.stream()
                .map(cartProduct -> {
                    // 상품 정보를 레포지토리에서 조회 (혹은 fetch join 사용권장)
                    Product product = productRepository.findById(cartProduct.getProductId())
                            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

                    return GetAllCartProductResponse.from(cartProduct, product);
                }).toList();
        // 3. 응답 생성
        return CursorResponse.of(
                responsesList,
                size,
                dto -> dto.cartProductId()
        );


    }

}
