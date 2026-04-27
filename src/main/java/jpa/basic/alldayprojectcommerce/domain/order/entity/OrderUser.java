package jpa.basic.alldayprojectcommerce.domain.order.entity;

import jakarta.persistence.*;
import jpa.basic.alldayprojectcommerce.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "order_users",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_order_users_order_id",
                columnNames = {"order_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderUser extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, length = 100)
    private String phone;

    @Column(nullable = false)
    private String address;

    @Builder
    public OrderUser(Long orderId, String name, String phone, String address) {
        this.orderId = orderId;
        this.name = name;
        this.phone = phone;
        this.address = address;
    }
}
