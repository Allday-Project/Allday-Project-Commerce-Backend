package jpa.basic.alldayprojectcommerce.domain.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SearchProductRequest(

        @NotBlank(message = "검색어는 필수입니다.")
        @Size(min = 2, message = "검색어는 최소 2글자 이상입니다.")
        String keyword
) {}
