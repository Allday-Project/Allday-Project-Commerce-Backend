package jpa.basic.alldayprojectcommerce.domain.order.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OrderCreateRequest(
        @NotBlank
        Long productId,
        @NotBlank
        Integer quantity
) {
}
