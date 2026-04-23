package jpa.basic.alldayprojectcommerce.domain.order.dto.response;

import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderStatus;

public record EventOrderResponse(String orderUid,
                                 OrderStatus status) {
    public static EventOrderResponse from(String orderUid, OrderStatus status) {
        return new EventOrderResponse(orderUid, status);
    }
}
