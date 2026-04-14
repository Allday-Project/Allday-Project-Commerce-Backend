package jpa.basic.alldayprojectcommerce.domain.order.dto.request;

import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderItem;

import java.util.List;

public record CreateOrderRequest(
        Long userId,
        List<OrderItem> orderItems
) {
}