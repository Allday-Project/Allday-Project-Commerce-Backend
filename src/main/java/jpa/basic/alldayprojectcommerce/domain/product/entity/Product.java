package jpa.basic.alldayprojectcommerce.domain.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
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
}


