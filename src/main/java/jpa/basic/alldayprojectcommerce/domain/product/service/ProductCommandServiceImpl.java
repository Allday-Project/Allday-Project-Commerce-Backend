package jpa.basic.alldayprojectcommerce.domain.product.service;

import jpa.basic.alldayprojectcommerce.common.lock.annotation.RedissonLock;
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
//    @RedissonLock(
//            key = "'lock:product:' + #productId",
//            waitTimeSeconds = 1,
//            leaseTimeSeconds = 3
//    )
    public void decreaseStock(Long productId, int quantity, Long orderId) {
        if (quantity <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        int updatedRows = productRepository.decreaseStockIfAvailable(productId, quantity);

        if (updatedRows == 0) {
            throw new CustomException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }

//        Integer currentStock = productRepository.findStockByProductId(productId);
//
//        if (currentStock == null) {
//            throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
//        }

        // TODO : 고트래픽 상황에서는 비동기 이벤트로 빼는 방법 고려하기. 추후 개선사항
//        saveStockHistory(productId, -quantity, currentStock, orderId);
    }




    // 재고를 증가시킨다.
    @Override
    public void increaseStock(Long productId, int quantity, Long orderId) {
        if (quantity <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        int updatedRows = productRepository.increaseStock(productId, quantity);

        if (updatedRows == 0) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        Integer currentStock = productRepository.findStockByProductId(productId);

        if (currentStock == null) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        saveStockHistory(productId, quantity, currentStock, orderId);
    }

    public void saveStockHistory(Long productId, int quantity, int currentStock, Long orderId) {
        Stock stock = Stock.builder()
                .productId(productId)
                .changeStock(quantity)
                .stock(currentStock)
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

}