package jpa.basic.alldayprojectcommerce.domain.product.service;

import jakarta.transaction.Transactional;
import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Stock;
import jpa.basic.alldayprojectcommerce.domain.product.repository.ProductRepository;
import jpa.basic.alldayprojectcommerce.domain.product.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductCommandServiceImpl implements ProductCommandService{

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    // 결재가 완료 되면 재고 차감

    // 재고를 차감 한다.
    @Override
    @Transactional
    public void decreaseStockOnPayment(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        product.decreaseStock(quantity);
    }

    // 재고를 증가시킨다.
    @Override
    @Transactional
    public void increaseStockOnCancel(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        product.increaseStock(quantity);
    }

    // 재고를 재고 관리 테이블에 기록한다.
    @Override
    public void saveStockHistory(Long productId, Long orderId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        Stock stock = Stock.builder()
                .productId(productId)
                .changeStock(quantity)
                .stock(product.getStock())
                .orderId(orderId)
                .build();
        stockRepository.save(stock);
    }

}
