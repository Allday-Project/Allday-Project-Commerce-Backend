package jpa.basic.alldayprojectcommerce.domain.order.service.event;

import jpa.basic.alldayprojectcommerce.domain.order.dto.response.EventOrderResponse;

public interface EventOrderOptimisticLockService {

    EventOrderResponse createEventOrderWithOptimisticLockRetry(Long productId, Long userId);
}