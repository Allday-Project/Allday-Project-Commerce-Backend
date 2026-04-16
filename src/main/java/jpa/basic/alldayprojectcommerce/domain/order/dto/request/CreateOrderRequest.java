package jpa.basic.alldayprojectcommerce.domain.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotEmpty
        @Valid
        List<OrderItemRequest> orderItems
) {}

