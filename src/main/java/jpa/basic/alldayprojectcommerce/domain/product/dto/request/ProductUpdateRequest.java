package jpa.basic.alldayprojectcommerce.domain.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Category;

public record ProductUpdateRequest(
        @NotBlank
        String name,
        @NotNull @Min(0)
        Long price,
        @Min(0)
        int stock,
        @NotBlank
        String description,
        @NotNull
        Category category,
        String imageUrl
) {
}