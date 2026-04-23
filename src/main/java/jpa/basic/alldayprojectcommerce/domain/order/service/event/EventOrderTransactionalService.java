package jpa.basic.alldayprojectcommerce.domain.order.service.event;

import jpa.basic.alldayprojectcommerce.domain.order.dto.response.EventOrderResponse;

public interface EventOrderTransactionalService {
    EventOrderResponse createEventOrderInNewTransaction(Long productId, Long userId);
}