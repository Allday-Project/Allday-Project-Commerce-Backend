package jpa.basic.alldayprojectcommerce.domain.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Category;
import jpa.basic.alldayprojectcommerce.domain.product.entity.ProductStatus;

public record SearchProductRequest(
        @NotBlank(message = "검색어는 필수입니다.")
        @Size(min = 2, message = "검색어는 최소 2글자 이상입니다.")
        String keyword

) {}
public class SearchProductRequest {
    private String keyword;
    private Category category;
    private ProductStatus status;
    private String Sort;
}
