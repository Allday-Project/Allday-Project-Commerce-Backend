package jpa.basic.alldayprojectcommerce.domain.product.dto.response;

import jpa.basic.alldayprojectcommerce.domain.product.entity.ProductKeyword;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SearchProductResponse(

        String keyword,
        Long searchCount,
        LocalDate searchDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static  SearchProductResponse from(ProductKeyword productKeyword) {
        return new SearchProductResponse(
                productKeyword.getKeyword(),
                productKeyword.getSearchCount(),
                productKeyword.getSearchDate(),
                productKeyword.getCreatedAt(),
                productKeyword.getUpdatedAt()
        );
    }
}
