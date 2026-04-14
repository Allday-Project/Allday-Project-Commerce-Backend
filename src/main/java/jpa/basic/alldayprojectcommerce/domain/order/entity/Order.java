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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(nullable = false, length = 100)
    private String orderUid;
    @Column(nullable = false, length = 100, unique = true)
    private String orderNumber;
    @Column(nullable = false)
    private Long totalAmount;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Builder
    public Order(User user, String orderUid, String orderNumber, Long totalAmount, OrderStatus status) {
        this.user = user;
        this.orderUid = orderUid;
        this.orderNumber = orderNumber;
        this.totalAmount = (totalAmount == null) ? 0L : totalAmount;
        this.status = status;
    }
}