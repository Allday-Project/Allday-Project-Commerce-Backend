package jpa.basic.alldayprojectcommerce.domain.product.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import jpa.basic.alldayprojectcommerce.domain.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "stocks")
@NoArgsConstructor
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

    @Column(length = 20)
    @Size(max = 20)
    private String adminId;

    @Column(length = 255)
    private String reasonCode;


    @Builder
    public Stock(Long id, Long productId, int changeStock, int stock, Long orderId, String adminId, String reasonCode) {
        this.id = id;
        this.productId = productId;
        this.changeStock = changeStock;
        this.stock = stock;
        this.orderId = orderId;
        this.adminId = adminId;
        this.reasonCode = reasonCode;
    }
}



