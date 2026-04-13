package jpa.basic.alldayprojectcommerce.domain.order.entity;

public record OrderItem(
        Long productId,
        int quantity
) {
}