package jpa.basic.alldayprojectcommerce.domain.product.dto.request;

import jpa.basic.alldayprojectcommerce.domain.product.entity.Category;
import jpa.basic.alldayprojectcommerce.domain.product.entity.ProductStatus;

public class SearchProductRequest {
    private String keyword;
    private Category category;
    private ProductStatus status;
    private String Sort;
}
