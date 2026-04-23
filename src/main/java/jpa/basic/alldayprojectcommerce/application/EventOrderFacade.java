package jpa.basic.alldayprojectcommerce.application;

import jpa.basic.alldayprojectcommerce.domain.order.dto.response.EventOrderResponse;
import jpa.basic.alldayprojectcommerce.domain.order.service.EventOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventOrderFacade {

    private final EventOrderService eventOrderService;


    public EventOrderResponse createEventOrderWithoutLock(Long productId, Long userId) {
        return eventOrderService.createEventOrder(productId, userId);
    }
}
