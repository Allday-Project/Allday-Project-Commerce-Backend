package jpa.basic.alldayprojectcommerce.domain.order.dto.response;

public record CreateOrderResponse(
        String orderRef,
        String orderNumber,
        Long totalAmount
) {
}