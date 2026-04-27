package jpa.basic.alldayprojectcommerce.domain.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.BaseEntity;
import lombok.*;

@Getter
@Entity
@Table(
        name = "products",
        indexes = {
                // 단일 인덱스 : price 범위 검색 최적화
                @Index(name = "idx_products_price", columnList = "price"),
                // 단일 인덱스 : name 전방 검색 최적화 (LIKE '검색어%')
                @Index(name = "idx_products_name", columnList = "name"),
                // 복합 인덱스 : status 필터 + id 정렬 (페이징 쿼리 최적화)
                @Index(name = "idx_products_status_id", columnList = "status, id"),
                // 복합 인덱스 : category + status 동시 필터
                @Index(name = "idx_products_category_status", columnList = "category, status")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    @Min(0)
    private Long price;

    @Column(nullable = false)
    @Min(0)
    private int stock;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    private String imageUrl;


    @Builder
    private Product(String name, Long price, int stock, String description, ProductStatus status, Category category, String imageUrl) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.status = status;
        this.category = category;
        this.imageUrl = imageUrl;
    }


    public void update(String name, Long price, int stock, String description, Category category, String imageUrl) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.category = category;
        this.imageUrl = imageUrl;
        closeSales();
        resumeSales();
    }


    // 재고 차감 로직
    public void decreaseStock(int quantity) {
        validQuantity(quantity);
        verifyStock(quantity);
        this.stock -= quantity;
        closeSales();
    }

    // 재고 증가 로직
    public void increaseStock(int quantity) {
        validQuantity(quantity);
        this.stock += quantity;
        resumeSales();
    }

    public void checkAvailability(int quantity) {
        verifyStock(quantity);
        validQuantity(quantity);
    }

    // 입력 수량이 재고보다 클 때 에러 날림
    private void verifyStock(int quantity) {
        if (this.stock < quantity) {
            throw new CustomException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }
    }

    // 재고가 null 이거나 입력 수량이 0보다 작거나 같을 때 에러 날림
    private void validQuantity(int quantity) {
        if (quantity <= 0) {
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


