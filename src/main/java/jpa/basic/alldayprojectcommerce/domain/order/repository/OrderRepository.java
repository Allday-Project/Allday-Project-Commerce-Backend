package jpa.basic.alldayprojectcommerce.domain.order.repository;

import jakarta.persistence.LockModeType;
import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderUid(String orderUid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.orderUid = :orderUid")
    Optional<Order> findByOrderUidForUpdate(@Param("orderUid") String orderUid);
}
