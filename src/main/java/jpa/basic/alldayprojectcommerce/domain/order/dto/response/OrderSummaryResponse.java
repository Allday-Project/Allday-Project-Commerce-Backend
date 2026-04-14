package jpa.basic.alldayprojectcommerce.domain.order.dto.response;

import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderStatus;

import java.time.LocalDateTime;

public record OrderSummaryResponse(
        String orderUid,
        String orderNumber,
        Long totalAmount,
        OrderStatus orderStatus,
        LocalDateTime createdAt
) {
}