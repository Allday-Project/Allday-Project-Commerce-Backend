package jpa.basic.alldayprojectcommerce.domain.cartProduct.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jpa.basic.alldayprojectcommerce.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "cart_products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartProduct extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    @Builder
    public CartProduct (Long userId, Long productId, int quantity) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
    }

    // 수량 변경 (dirty checking 활용)
    public void updateQuantity (int quantity) {
        this.quantity = quantity;
    }
}
