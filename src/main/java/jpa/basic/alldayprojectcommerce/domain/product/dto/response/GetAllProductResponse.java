package jpa.basic.alldayprojectcommerce.domain.product.dto.response;

import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import jpa.basic.alldayprojectcommerce.domain.product.entity.ProductCategory;
import jpa.basic.alldayprojectcommerce.domain.product.entity.ProductStatus;

public record GetAllProductResponse(
        Long id,
        String name,
        Long price,
        ProductStatus status,
        ProductCategory category
) {

    public static GetAllProductResponse from(Product product) {
        return new GetAllProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStatus(),
                product.getCategory()
        );
    }
}