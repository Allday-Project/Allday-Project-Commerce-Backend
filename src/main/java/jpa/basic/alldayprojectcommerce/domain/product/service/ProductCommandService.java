package jpa.basic.alldayprojectcommerce.domain.product.service;

import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;

public interface ProductCommandService {


    void decreaseStockOnPayment(Long productId, int quantity);
    void increaseStockOnCancel(Long productId, int quantity);
    void saveStockHistory(Long productId, Long orderId, int quantity);
}
