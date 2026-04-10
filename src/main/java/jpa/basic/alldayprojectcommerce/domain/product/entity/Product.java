package jpa.basic.alldayprojectcommerce.domain.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jpa.basic.alldayprojectcommerce.domain.BaseEntity;
import lombok.*;

@Getter
@Entity
@Table(name = "products")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String productName;

    @NotBlank
    @Column(nullable = false)
    private Long price;

    @NotBlank
    @Column(nullable = false)
    private Integer stock;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotBlank
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @NotBlank
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private Category category;
}
