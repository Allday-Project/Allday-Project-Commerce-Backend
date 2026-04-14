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
    @Column(unique = true, nullable = false, length = 30)
    private String orderUid;
    @Column(nullable = false)
    private Long totalAmount;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Builder
    public Order(User user, String orderUid, Long totalAmount, OrderStatus status) {
        this.user = user;
        this.orderUid = orderUid;
        this.totalAmount = (totalAmount == null) ? 0L : totalAmount;
        this.status = status;
    }
}