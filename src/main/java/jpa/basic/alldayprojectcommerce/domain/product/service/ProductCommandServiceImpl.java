package jpa.basic.alldayprojectcommerce.domain.product.service;

import jpa.basic.alldayprojectcommerce.domain.product.entity.ProductStatus;
import jpa.basic.alldayprojectcommerce.domain.product.dto.request.ProductUpdateRequest;
import jpa.basic.alldayprojectcommerce.domain.product.dto.response.ProductUpdateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Stock;
import jpa.basic.alldayprojectcommerce.domain.product.repository.ProductRepository;
import jpa.basic.alldayprojectcommerce.domain.product.repository.StockRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductCommandServiceImpl implements ProductCommandService {

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;


    // 재고를 차감 한다.
    @Override
    public void decreaseStock(Long productId, int quantity, Long orderId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        product.decreaseStock(quantity);
        saveStockHistory(product, quantity, orderId);
    }

    // 재고를 차감 한다.
    @Override
    public Product decreaseStockWithPessimisticLock(Long productId, int quantity) {
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getStatus() != ProductStatus.ON_SALE) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_ON_SALE);
        }

        if (product.getStock() < quantity) {
            throw new CustomException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }

        product.decreaseStock(quantity);
        // TODO 비동기 방식으로 처리하기. 동시성 문제의 성능 개선을 위함
//        saveStockHistory(product, quantity, orderId);
        return product;
    }

    // 재고를 증가시킨다.
    @Override
    public void increaseStock(Long productId, int quantity, Long orderId) {
        // TODO :  재고 증가 메서드인데 일반 조회 사용중. 추후 동시성 문제 해결 부분에서 비관적 락 적용 고려

        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        product.increaseStock(quantity);
        saveStockHistory(product, -quantity, orderId);

    }

    // 재고를 재고 관리 테이블에 기록한다.
    @Override
    public void saveStockHistory(Product product, int quantity, Long orderId) {

        Stock stock = Stock.builder()
                .productId(product.getId())
                .changeStock(quantity)
                .stock(product.getStock())
                .orderId(orderId)
                .build();
        stockRepository.save(stock);
    }

    @Override
    public void checkStock(Long productId, int quantity){
        Product product =  productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        product.checkAvailability(quantity);
    }


    @Override
    @Caching(evict = {
    @CacheEvict(value = "productDetail", key = "'product:' + #productId"),
    @CacheEvict(value = "productSearch", allEntries = true)
    })
    public ProductUpdateResponse updateProduct(Long productId, ProductUpdateRequest request){
        Product product =  productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        product.update(
                request.name(),
                request.price(),
                request.stock(),
                request.description(),
                request.category(),
                request.imageUrl());

        log.info("[캐시 갱신] productId: {}, name: {}", productId, request.name());
        log.info("[캐시 삭제] productId: {}", productId);

        return ProductUpdateResponse.from(product);
    }
}