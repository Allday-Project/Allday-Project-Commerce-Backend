package jpa.basic.alldayprojectcommerce.domain.order.dto.response;

import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;
import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderItem;
import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderStatus;
import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderUser;

import java.time.LocalDateTime;
import java.util.List;

// 주문 상세 조회 응답
public record GetOrderDetailsResponse(
        String orderUid,
        LocalDateTime orderDate,
        OrderStatus status,
        Long totalAmount,
        Long deliveryFee,
        Long finalAmount,
        OrdererInfo ordererInfo,        // 결제 완료 후 고객 스냅샷
        List<OrderItemDetail> items
) {

    private static final Long DELIVERY_FEE = 3_000L;

    public record OrdererInfo(
            String name,
            String phone,
            String address
    ) {}

    public record OrderItemDetail(
            String productName,
            int quantity,
            Long productPrice,
            Long itemAmount
    ) {}

    public static GetOrderDetailsResponse from(Order order, OrderUser orderUser, List<OrderItem> items) {
        OrdererInfo ordererInfo = new OrdererInfo(
                orderUser.getName(),
                orderUser.getPhone(),
                orderUser.getAddress()
        );

        List<OrderItemDetail> detail = items.stream()
                .map(item -> new OrderItemDetail(
                        item.getProductName(),
                        item.getQuantity(),
                        item.getProductPrice(),
                        item.getProductPrice() * item.getQuantity()
                )).toList();

        return new GetOrderDetailsResponse(
                order.getOrderUid(),
                order.getCreatedAt(),
                order.getStatus(),
                order.getTotalAmount(),
                DELIVERY_FEE,
                order.getTotalAmount() + DELIVERY_FEE,
                ordererInfo,
                detail
        );
    }
}
