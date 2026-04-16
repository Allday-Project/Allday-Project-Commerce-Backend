package jpa.basic.alldayprojectcommerce.domain.order.repository;

import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // 주문 ID에 해당하는 모든 상품 목록 조회
    List<OrderItem> findByOrderId(Long orderId);
}
