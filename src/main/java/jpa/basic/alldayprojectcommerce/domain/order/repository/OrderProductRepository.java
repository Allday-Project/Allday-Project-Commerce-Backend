package jpa.basic.alldayprojectcommerce.domain.order.repository;

import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderProduct;
import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {

    // 주문 ID에 해당하는 모든 상품 목록 조회
    List<OrderProduct> findByOrderId(Long orderId);

    // N+1 문제 - 여러 주문 ID의 상품을 한 번에 조회
    List<OrderProduct> findByOrderIdIn(Collection<Long> orderIds);

    @Query("""
    select case when count(op) > 0 then true else false end
    from OrderProduct op
    join Order o on o.id = op.orderId
    where op.productId = :productId
      and o.userId = :userId
      and o.status = :status
""")
    boolean existsCompletedEventOrder(
            @Param("productId") Long productId,
            @Param("userId") Long userId,
            @Param("status") OrderStatus status
    );

}
