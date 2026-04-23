package jpa.basic.alldayprojectcommerce.domain.order.service.event;

import jpa.basic.alldayprojectcommerce.domain.order.dto.response.EventOrderResponse;

public interface EventOrderService {
    EventOrderResponse createEventOrder(Long productId, Long userId);

}
