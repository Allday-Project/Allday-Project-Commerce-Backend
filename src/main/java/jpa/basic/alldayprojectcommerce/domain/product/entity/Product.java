package jpa.basic.alldayprojectcommerce.domain.product.entity;

import jakarta.persistence.*;
import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.BaseEntity;
import lombok.*;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;

    private String imageUrl;


    @Builder
    private Product(String name, Long price, int stock, String description, ProductStatus status, ProductCategory category, String imageUrl) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.status = status;
        this.category = category;
        this.imageUrl = imageUrl;
    }




    // 재고 차감 로직
    public void decreaseStock(Long quantity) {
        validQuantity(quantity);
        if (this.stock < quantity) {
            throw new CustomException(ErrorCode.OUT_OF_STOCK);
        }
        this.stock -= quantity;
        closeSales();
    }

    // 재고 증가 로직
    public void increaseStock(Long quantity) {
        validQuantity(quantity);
        this.stock += quantity;
        resumeSales();
    }


    private void validQuantity(Long quantity) {
        if (quantity == null || quantity <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    // 재고가 0이 되면 품절 상태로 변경
    private void closeSales() {
        if (this.status == ProductStatus.SOLD_OUT) {
            return;
        }
        if (this.stock == 0) {
            this.status = ProductStatus.SOLD_OUT;
        }
    }

    // 재고를 0에서 올렸을 때 판매 중 상태로 변경
    private void resumeSales() {
        if (this.status == ProductStatus.SOLD_OUT && this.stock > 0) {
            this.status = ProductStatus.ON_SALE;
        }
    }
}


