package jpa.basic.alldayprojectcommerce.domain.product.service;

import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;

public interface ProductCommandService {
    void decreaseStock(Order order);
}
