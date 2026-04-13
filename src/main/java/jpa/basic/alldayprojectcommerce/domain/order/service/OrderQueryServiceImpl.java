package jpa.basic.alldayprojectcommerce.domain.order.service;

import jpa.basic.alldayprojectcommerce.domain.order.dto.response.OrderDetailResponse;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.OrderSummaryResponse;
import jpa.basic.alldayprojectcommerce.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderRepository orderRepository;

    @Override
    public OrderSummaryResponse getAll(Long userId) {
        // TODO
        return null;
    }

    @Override
    public OrderDetailResponse get(String orderRef) {
        // TODO
        return null;
    }
}