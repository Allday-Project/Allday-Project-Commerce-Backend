package jpa.basic.alldayprojectcommerce.domain.cartProduct.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateQuantityRequest(
        @NotNull
        @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
        int quantity
) {}
