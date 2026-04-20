package jpa.basic.alldayprojectcommerce.domain.order.service;

import jakarta.transaction.Transactional;
import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;
import jpa.basic.alldayprojectcommerce.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderUserCommandServiceImpl implements OrderUserCommandService {
    @Override
    public void createSnapshot(Order order, User user) {

    }
}
