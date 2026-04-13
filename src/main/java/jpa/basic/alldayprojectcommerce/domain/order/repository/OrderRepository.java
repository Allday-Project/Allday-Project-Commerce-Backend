package jpa.basic.alldayprojectcommerce.domain.order.repository;

import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderUid(String orderUid);
}
