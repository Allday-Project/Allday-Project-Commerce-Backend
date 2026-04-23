package jpa.basic.alldayprojectcommerce.domain.product.entity;


import jakarta.persistence.*;
import jpa.basic.alldayprojectcommerce.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "product_keywords")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductKeyword extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String keyword;

    @Column(nullable = false)
    private Long searchCount;

    @Column(nullable = false)
    private LocalDate searchDate;

    @Builder
    private ProductKeyword(String keyword, Long searchCount, LocalDate searchDate) {
        this.keyword = keyword;
        this.searchCount = searchCount;
        this.searchDate = searchDate;
    }

    public static ProductKeyword of(String keyword) {
        return ProductKeyword.builder()
                .keyword(keyword)
                .searchCount(1L)
                .searchDate(LocalDate.now())
                .build();
    }
}
