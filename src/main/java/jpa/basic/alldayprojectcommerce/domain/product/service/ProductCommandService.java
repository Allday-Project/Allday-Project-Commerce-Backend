package jpa.basic.alldayprojectcommerce.domain.product.service;

import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;

public interface ProductCommandService {
    void decreaseStock(Order order);


    void decreaseStock(Long productId, int quantity);
    void increaseStock(Long productId, int quantity);
    void saveStockHistory(Long productId, Long orderId, int quantity);
}
