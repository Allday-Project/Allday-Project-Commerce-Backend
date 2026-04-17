package jpa.basic.alldayprojectcommerce.domain.order.dto.response;

import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;
import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderItem;
import jpa.basic.alldayprojectcommerce.domain.user.entity.User;

import java.util.List;

// 주문서 조회 응답
public record GetOneOrderResponse(
        String orderUid,
        Long totalAmount,
        Long deliveryFee,
        Long finalAmount,   // 상품 금액 + 배송비
        List<OrderItemInfo> items
) {

    // 배송비 3,000원 고정
    private static final Long DELIVERY_FEE = 3_000L;

    public record OrderItemInfo(
            Long productId,
            String productName,
            int quantity,
            Long productPrice,
            Long itemAmount
    ) {}

    public static GetOneOrderResponse from(Order order, List<OrderItem> items) {
        List<OrderItemInfo> info = items.stream()
                .map(item -> new OrderItemInfo(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getProductPrice(),
                        item.getProductPrice() * item.getQuantity()
                )).toList();

        return new GetOneOrderResponse(
                order.getOrderUid(),
                order.getTotalAmount(),
                DELIVERY_FEE,
                order.getTotalAmount() + DELIVERY_FEE,
                info
        );
    }
}
