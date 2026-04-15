package jpa.basic.alldayprojectcommerce.domain.order.entity;

import jakarta.persistence.*;
import jpa.basic.alldayprojectcommerce.domain.BaseEntity;
import jpa.basic.alldayprojectcommerce.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 30, unique = true)
    private String orderUid;

    @Column(nullable = false)
    private Long totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Builder
    public Order(Long userId, String orderUid, Long totalAmount, OrderStatus status) {
        this.userId = userId;
        this.orderUid = orderUid;
        this.totalAmount = (totalAmount == null) ? 0L : totalAmount;
        this.status = status;
    }
}
