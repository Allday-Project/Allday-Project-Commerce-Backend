package jpa.basic.alldayprojectcommerce.domain.order.service;

import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;
import jpa.basic.alldayprojectcommerce.domain.user.entity.User;

public interface OrderUserCommandService {

    void createSnapshot(Order order, User user);
}
