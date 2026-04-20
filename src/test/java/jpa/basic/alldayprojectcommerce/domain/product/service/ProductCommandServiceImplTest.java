package jpa.basic.alldayprojectcommerce.domain.product.service;

import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import jpa.basic.alldayprojectcommerce.domain.product.entity.ProductStatus;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Stock;
import jpa.basic.alldayprojectcommerce.domain.product.repository.ProductRepository;
import jpa.basic.alldayprojectcommerce.domain.product.repository.StockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductCommandServiceImplTest {


    @InjectMocks
    private ProductCommandServiceImpl productCommandService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockRepository stockRepository;


    // ✅ 1. 정상 차감
    @Test
    @DisplayName("결제 시 재고가 정상적으로 차감되어야 한다")
    void decreaseStock_Success() {
        // given
        Long productId = 1L;
        int decreaseQuantity = 5;

        Product mockProduct = Product.builder()
                .stock(10)
                .build();

        ReflectionTestUtils.setField(mockProduct, "id", productId); // id 강제 주입

        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));
//        when(productRepository.save(any(Product.class))).thenReturn(mockProduct); // save 스터빙

        // when
        productCommandService.decreaseStock(productId, decreaseQuantity);

        // then
        assertEquals(5, mockProduct.getStock());
        verify(productRepository, times(1)).findById(productId);
//        verify(productRepository, times(1)).save(mockProduct); // ✅ save 검증 추가
    }


    // 2. 재고 부족 예외
    @Test
    @DisplayName("재고가 부족하면 예외가 발생해야 한다")
    void decreaseStockOnPayment_InsufficientStock() {
        Long productId = 1L;

        Product mockProduct = Product.builder()
                .stock(10)
                .build();
        ReflectionTestUtils.setField(mockProduct, "id", productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        // ✅ CustomException으로 변경
        CustomException ex = assertThrows(CustomException.class, () ->
                productCommandService.decreaseStock(productId, 15)
        );

        // ✅ ErrorCode까지 검증하면 더 정확함
        assertEquals(ErrorCode.PRODUCT_OUT_OF_STOCK, ex.getErrorCode());
    }


    // 3. 상품 미존재 예외
    @Test
    @DisplayName("존재하지 않는 상품이면 예외가 발생해야 한다")
    void decreaseStock_ProductNotFound() {
        Long productId = 999L;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // ✅ CustomException으로 변경
        CustomException ex = assertThrows(CustomException.class, () ->
                productCommandService.decreaseStock(productId, 5)
        );

        assertEquals(ErrorCode.PRODUCT_NOT_FOUND, ex.getErrorCode());
    }


    // 4. ✅ 보너스 - 재고가 0이 되면 SOLD_OUT 상태로 변경되는지 검증
    @Test
    @DisplayName("재고가 0이 되면 품절 상태로 변경되어야 한다")
    void decreaseStock_SoldOut() {
        Long productId = 1L;

        Product mockProduct = Product.builder()
                .stock(5)
                .status(ProductStatus.ON_SALE)
                .build();
        ReflectionTestUtils.setField(mockProduct, "id", productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        productCommandService.decreaseStock(productId, 5); // 재고 전부 소진

        assertEquals(0, mockProduct.getStock());
        assertEquals(ProductStatus.SOLD_OUT, mockProduct.getStatus()); // ✅ 품절 상태 검증
    }


    // ✅ 재고 증가 - 정상
    @Test
    @DisplayName("취소 시 재고가 정상적으로 증가되어야 한다")
    void increaseStock_Success() {
        // given
        Long productId = 1L;
        int increaseQuantity = 5;

        Product mockProduct = Product.builder()
                .stock(10)
                .build();
        ReflectionTestUtils.setField(mockProduct, "id", productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        // when
        productCommandService.increaseStock(productId, increaseQuantity);

        // then
        assertEquals(15, mockProduct.getStock()); // 10 + 5 = 15
        verify(productRepository, times(1)).findById(productId);
    }


    // ✅ 재고 증가 - 상품 미존재 예외
    @Test
    @DisplayName("취소 시 존재하지 않는 상품이면 예외가 발생해야 한다")
    void increaseStock_ProductNotFound() {
        // given
        Long productId = 999L;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // when & then
        CustomException ex = assertThrows(CustomException.class, () ->
                productCommandService.increaseStock(productId, 5)
        );

        assertEquals(ErrorCode.PRODUCT_NOT_FOUND, ex.getErrorCode());
    }


    // ✅ 재고 이력 저장 - 정상
    @Test
    @DisplayName("재고 이력이 정상적으로 저장되어야 한다")
    void saveStockHistory_Success() {
        // given
        Long productId = 1L;
        Long orderId = 100L;
        int quantity = 5;

        Product mockProduct = Product.builder()
                .stock(15)
                .build();
        ReflectionTestUtils.setField(mockProduct, "id", productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        // when
        productCommandService.saveStockHistory(productId, orderId, quantity);

        // then
        verify(stockRepository, times(1)).save(any(Stock.class));
    }


    // ✅ 재고 이력 저장 - 상품 미존재 예외
    @Test
    @DisplayName("재고 이력 저장 시 존재하지 않는 상품이면 예외가 발생해야 한다")
    void saveStockHistory_ProductNotFound() {
        // given
        Long productId = 999L;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // when & then
        CustomException ex = assertThrows(CustomException.class, () ->
                productCommandService.saveStockHistory(productId, 100L, 5)
        );

        assertEquals(ErrorCode.PRODUCT_NOT_FOUND, ex.getErrorCode());
    }
}