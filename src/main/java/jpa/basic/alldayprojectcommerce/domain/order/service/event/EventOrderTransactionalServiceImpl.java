package jpa.basic.alldayprojectcommerce.domain.order.service.event;


import jpa.basic.alldayprojectcommerce.domain.order.dto.response.EventOrderResponse;
import jpa.basic.alldayprojectcommerce.domain.order.service.event.EventOrderService;
import jpa.basic.alldayprojectcommerce.domain.order.service.event.EventOrderTransactionalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventOrderTransactionalServiceImpl implements EventOrderTransactionalService {

    private final EventOrderService eventOrderService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EventOrderResponse createEventOrderInNewTransaction(Long productId, Long userId) {
        return eventOrderService.createEventOrder(productId, userId);
    }
}