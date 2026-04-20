package jpa.basic.alldayprojectcommerce.domain.product.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Stock;
import jpa.basic.alldayprojectcommerce.domain.product.repository.ProductRepository;
import jpa.basic.alldayprojectcommerce.domain.product.repository.StockRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductCommandServiceImpl implements ProductCommandService {

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;


    // 재고를 차감 한다.
    @Override
    public void decreaseStock(Long productId, int quantity, Long orderId) {
        // TODO :  재고 증가 메서드인데 일반 조회 사용중. 추후 동시성 문제 해결 부분에서 비관적 락 적용 고려
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        product.decreaseStock(quantity);
        saveStockHistory(product, quantity, orderId);
    }

    // 재고를 증가시킨다.
    @Override
    public void increaseStock(Long productId, int quantity, Long orderId) {
        // TODO :  재고 증가 메서드인데 일반 조회 사용중. 추후 동시성 문제 해결 부분에서 비관적 락 적용 고려

        Product product = productRepository.findById(productId)
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

}