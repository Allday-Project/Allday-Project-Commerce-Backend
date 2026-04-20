package jpa.basic.alldayprojectcommerce.domain.product.service;

import jakarta.transaction.Transactional;
import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;
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
public class ProductCommandServiceImpl implements ProductCommandService{
    @Override
    public void decreaseStock(Order order) {

    }

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;


    // 재고를 차감 한다.
    @Override
    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        product.decreaseStock(quantity);
    }

    // 재고를 증가시킨다.
    @Override
    @Transactional
    public void increaseStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        product.increaseStock(quantity);
    }

    // 재고를 재고 관리 테이블에 기록한다.
    @Override
    @Transactional
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