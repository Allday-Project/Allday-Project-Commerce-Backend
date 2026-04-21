package jpa.basic.alldayprojectcommerce.domain.order.dto.response;

public record OrderProductInfo(
        Long productId,
        int quantity
) {
}