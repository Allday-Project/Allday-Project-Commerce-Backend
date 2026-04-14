package jpa.basic.alldayprojectcommerce.domain.order.service;

import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;

public interface OrderQueryService {


    Order getOrderByOrderUid(String orderUid);

    Order getOrderByOrderUidForUpdate(String orderUid);
}