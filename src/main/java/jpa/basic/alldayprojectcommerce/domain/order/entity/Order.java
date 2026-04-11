package jpa.basic.alldayprojectcommerce.domain.order.entity;

import jakarta.persistence.*;
import jpa.basic.alldayprojectcommerce.domain.BaseEntity;
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

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String orderUid;

    @Column(nullable = false, length = 100, unique = true)
    private String orderNumber;

    @Column(nullable = false)
    private Long totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Builder
    public Order(Long userId, String orderUid, String orderNumber, Long totalAmount, OrderStatus status) {
        this.userId = userId;
        this.orderUid = orderUid;
        this.orderNumber = orderNumber;
        this.totalAmount = (totalAmount == null) ? 0L : totalAmount;
        this.status = status;
    }
}
