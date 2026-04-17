package jpa.basic.alldayprojectcommerce.domain.product.service;

public interface ProductCommandService {


    void decreaseStockOnPayment(Long productId, int quantity);
    void increaseStockOnCancel(Long productId, int quantity);
    void saveStockHistory(Long productId, Long orderId, int quantity);
}
