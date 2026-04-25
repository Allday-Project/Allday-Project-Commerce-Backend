package jpa.basic.alldayprojectcommerce.domain.product.dto.response;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Category;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;

public record ProductUpdateResponse(
        @NotBlank
        String name,
        @NotNull @Min(0)
        Long price,
        @NotNull @Min(0)
        int stock,
        @NotBlank
        String description,
        @NotNull
        Category category,
        String imageUrl
) {
    public static  ProductUpdateResponse from(Product product) {
        return new ProductUpdateResponse(
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getDescription(),
                product.getCategory(),
                product.getImageUrl()
        );
    }
}
