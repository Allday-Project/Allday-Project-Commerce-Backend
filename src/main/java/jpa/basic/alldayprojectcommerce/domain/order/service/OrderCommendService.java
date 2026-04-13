package jpa.basic.alldayprojectcommerce.domain.order.service;

import jpa.basic.alldayprojectcommerce.domain.order.dto.response.CreateOrderResponse;
import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderItem;

import java.util.List;

public interface OrderCommendService {

    CreateOrderResponse create(Long userId, List<OrderItem> orderItems);
}