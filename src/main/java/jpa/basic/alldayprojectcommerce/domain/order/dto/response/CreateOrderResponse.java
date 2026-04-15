package jpa.basic.alldayprojectcommerce.domain.order.dto.response;

public record CreateOrderResponse(
        String orderUid,
        Long totalAmount
) {
}
