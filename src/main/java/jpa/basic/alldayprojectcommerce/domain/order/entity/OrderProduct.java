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
        name = "order_products",
        indexes = {
                // findByOrderId, findByOrderIn
                @Index(name = "idx_order_products_order_id", columnList = "order_id"),
                // 이벤트 상품 중복 체크용
                @Index(name = "idx_order_products_product_id", columnList = "product_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderProduct extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long productId;

    // 스냅샷
    @Column(nullable = false, length = 100)
    private String productName;

    // 스냅샷
    @Column(nullable = false)
    private Long productPrice;

    @Column(nullable = false)
    private int quantity;

    @Builder
    public OrderProduct(Long orderId, Long productId, String productName, Long productPrice, int quantity) {
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.quantity = quantity;
    }
}
