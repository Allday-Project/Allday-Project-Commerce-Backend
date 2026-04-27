package jpa.basic.alldayprojectcommerce.domain.product.service;


import jpa.basic.alldayprojectcommerce.common.RestPage;
import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.product.dto.request.SearchProductRequest;
import jpa.basic.alldayprojectcommerce.domain.product.dto.response.GetAllProductResponse;
import jpa.basic.alldayprojectcommerce.domain.product.dto.response.GetOneProductResponse;
import jpa.basic.alldayprojectcommerce.domain.product.dto.response.SearchProductResponse;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import jpa.basic.alldayprojectcommerce.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryServiceImpl implements ProductQueryService {

    private final ProductRepository productRepository;

    @Override
    @Cacheable(value = "productDetail", key = "'product:' + #productId")
    public GetOneProductResponse getOneProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        return GetOneProductResponse.getProduct(product);
    }

    // 전체 조회
    @Override
    public Page<GetAllProductResponse> getAllProduct(String category, String keyword, Pageable pageable){
        return productRepository.findAllProducts(category, keyword, pageable)
                .map(GetAllProductResponse::from);
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


    // 레디스 포함한 상품 검색
    @Override
    @Cacheable(value = "productSearch",
            key = "'product:' + #searchRequest.keyword() + ':' + #pageable.pageNumber + ':' + #pageable.pageSize",
            sync = true) // sync : 동일 키에 대한 동시 DB 조회를 막아줌

    public RestPage<SearchProductResponse> searchProductsV2(SearchProductRequest searchRequest, Pageable pageable) {

        log.info("[캐시 미스 V2] keyword: {}", searchRequest.keyword());

        Page<Product> products = productRepository.searchProduct(searchRequest, pageable);
        Page<SearchProductResponse> mappedPage = products.map(
                p -> new SearchProductResponse(p.getName(), 0L, LocalDate.now(), p.getCreatedAt(), p.getUpdatedAt()));

        return new RestPage<>(mappedPage);
    }
}
