package jpa.basic.alldayprojectcommerce.domain.product.service;


import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.product.dto.request.FilterProductRequest;
import jpa.basic.alldayprojectcommerce.domain.product.dto.request.SearchProductRequest;
import jpa.basic.alldayprojectcommerce.domain.product.dto.response.GetAllProductResponse;
import jpa.basic.alldayprojectcommerce.domain.product.dto.response.GetOneProductResponse;
import jpa.basic.alldayprojectcommerce.domain.product.dto.response.SearchProductResponse;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import jpa.basic.alldayprojectcommerce.domain.product.repository.ProductRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Getter
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryServiceImpl implements ProductQueryService {

    private final ProductRepository productRepository;

    @Override
    public GetOneProductResponse getOneProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        return GetOneProductResponse.getProduct(product);
    }

    // 전체 조회
    @Override
    public Page<GetAllProductResponse> getAllProduct(FilterProductRequest filterRequest, Pageable pageable) {
        if (filterRequest == null) {
            filterRequest = new FilterProductRequest(null, null);
        }
        Page<Product> products = productRepository.findAllProducts(filterRequest, pageable);
        return products.map(GetAllProductResponse::from);
    }

    @Override
    public Product getByProductId(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
    public List<Product> findAllByIds(List<Long> productIds) {
        return productRepository.findAllById(productIds);
    }


    @Override
    public Page<GetAllProductResponse> searchProducts(SearchProductRequest searchRequest, Pageable pageable) {

        Page<Product> products = productRepository.searchProduct(searchRequest, pageable);
        return products.map(GetAllProductResponse::from);
    }

    @Override
    @Transactional
    @Cacheable(value = "productSearchCache", key = "'search:' + #searchRequest.keyword() + ':' + #pageable.pageNumber + ':' + #pageable.pageSize", sync = true )
    // sync : 동일 키에 대한 동시 DB 조회를 막아줌
    public Page<SearchProductResponse> searchProductsV2(SearchProductRequest searchRequest, Pageable pageable){
        // TODO: searchProductV2 repo method is commented out - using findByKeyword as fallback
        log.info("[캐시 미스] keyword: {}", searchRequest.keyword());
        Page<Product> products = productRepository.searchProduct(searchRequest, pageable);
        return products.map(p -> new SearchProductResponse(p.getName(), 0L, java.time.LocalDate.now(), p.getCreatedAt(), p.getUpdatedAt()));
    }
}
