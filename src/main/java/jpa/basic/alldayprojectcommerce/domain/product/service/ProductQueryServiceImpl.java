package jpa.basic.alldayprojectcommerce.domain.product.service;


import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.product.dto.response.GetAllProductResponse;
import jpa.basic.alldayprojectcommerce.domain.product.dto.response.GetProductResponse;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import jpa.basic.alldayprojectcommerce.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryServiceImpl implements ProductQueryService {

    private final ProductRepository productRepository;

    public GetProductResponse getOneProduct(Long productId){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        return GetProductResponse.getProduct(product);
    }


    // 전체 조회
    public Page<GetAllProductResponse> getAllProduct(Pageable pageable){
        return productRepository.findAllProducts(pageable)
                .map(GetAllProductResponse::from);
    }
}
