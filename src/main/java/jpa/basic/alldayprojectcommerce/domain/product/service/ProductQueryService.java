package jpa.basic.alldayprojectcommerce.domain.product.service;

import jpa.basic.alldayprojectcommerce.domain.product.dto.response.GetAllProductResponse;
import jpa.basic.alldayprojectcommerce.domain.product.dto.response.GetProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductQueryService {

    GetProductResponse getOneProduct(Long productId);
    Page<GetAllProductResponse> getAllProduct(Pageable pageable);
}
