package jpa.basic.alldayprojectcommerce.domain.order.service;

import jpa.basic.alldayprojectcommerce.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderCommandServiceImpl implements OrderCommandService{

    private final OrderRepository orderRepository;
}
