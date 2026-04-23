package jpa.basic.alldayprojectcommerce.domain.product.dto.request;

import jpa.basic.alldayprojectcommerce.domain.product.entity.Category;
import jpa.basic.alldayprojectcommerce.domain.product.entity.ProductStatus;


public record FilterProductRequest(

        Category category,
        ProductStatus status
) {}




