package jpa.basic.alldayprojectcommerce.domain.product.dto.response;

import jpa.basic.alldayprojectcommerce.domain.product.entity.Category;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;

public record ProductUpdateResponse(
        String name,
        Long price,
        int stock,
        String description,
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
