package jpa.basic.alldayprojectcommerce.domain.product.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import jpa.basic.alldayprojectcommerce.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "Product_stock_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int changeStock;

    @Min(0)
    @Column(nullable = false)
    private int stock;

    private Long orderId;


    @Builder
    public Stock(Long id, Long productId, int changeStock, int stock, Long orderId) {
        this.id = id;
        this.productId = productId;
        this.changeStock = changeStock;
        this.stock = stock;
        this.orderId = orderId;
    }
}



