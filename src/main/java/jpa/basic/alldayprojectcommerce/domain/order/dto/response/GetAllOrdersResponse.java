package jpa.basic.alldayprojectcommerce.domain.order.dto.response;

import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;
import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderItem;
import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

// 주문 목록 조회 응답
public record GetAllOrdersResponse(
        Long orderId,       // 다음 커서값
        String orderUid,    // 주문 상세 진입용
        LocalDateTime orderDate,
        OrderStatus status,
        Long totalAmount,
        List<OrderItemSummary> items
) {
    public record OrderItemSummary(
            String productName,
            int quantity,
            Long itemAmount
    ) {}

    public static GetAllOrdersResponse from(Order order, List<OrderItem> items) {
        List<OrderItemSummary> summary = items.stream()
                .map(item -> new OrderItemSummary(
                        item.getProductName(),
                        item.getQuantity(),
                        item.getProductPrice() * item.getQuantity()
                )).toList();

        return new GetAllOrdersResponse(
                order.getId(),
                order.getOrderUid(),
                order.getCreatedAt(),
                order.getStatus(),
                order.getTotalAmount(),
                summary
        );
    }
}
