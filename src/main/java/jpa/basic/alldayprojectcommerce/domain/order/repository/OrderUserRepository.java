package jpa.basic.alldayprojectcommerce.domain.order.repository;

import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderUserRepository extends JpaRepository<OrderUser, Long> {

    // 결제 완료 후 스냅샷 조회
    Optional<OrderUser> findByOrderId(Long orderId);
}
