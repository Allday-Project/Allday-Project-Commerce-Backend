package jpa.basic.alldayprojectcommerce.domain.product.dto.response;


import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import jpa.basic.alldayprojectcommerce.domain.product.entity.ProductStatus;

public record GetOneProductResponse(
        Long id,
        String name,
        Long price,
        int stock,
        String description,
        ProductStatus status,
        String imageUrl
){

    public static GetOneProductResponse getProduct(Product product){
        return new GetOneProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getDescription(),
                product.getStatus(),
                product.getImageUrl()
        );
    }
}

