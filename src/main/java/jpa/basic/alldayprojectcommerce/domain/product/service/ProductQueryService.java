package jpa.basic.alldayprojectcommerce.domain.product.service;

import jpa.basic.alldayprojectcommerce.common.RestPage;
import jpa.basic.alldayprojectcommerce.domain.product.dto.request.FilterProductRequest;
import jpa.basic.alldayprojectcommerce.domain.product.dto.request.SearchProductRequest;
import jpa.basic.alldayprojectcommerce.domain.product.dto.response.GetAllProductResponse;
import jpa.basic.alldayprojectcommerce.domain.product.dto.response.GetOneProductResponse;
import jpa.basic.alldayprojectcommerce.domain.product.dto.response.SearchProductResponse;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductQueryService {

    GetOneProductResponse getOneProduct(Long productId);
    Page<GetAllProductResponse> getAllProduct(FilterProductRequest filterRequest, Pageable pageable);

    // 상품 단건 조회 - 주문 생성에서 사용
    Product getByProductId(Long productId);

    List<Product> findAllByIds(List<Long> productIds);

    Page<GetAllProductResponse> searchProducts(SearchProductRequest searchRequest, Pageable pageable);

    Page<SearchProductResponse> searchProductsV2(SearchProductRequest searchRequest, Pageable pageable);
}
