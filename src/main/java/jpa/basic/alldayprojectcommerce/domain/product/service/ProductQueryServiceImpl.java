package jpa.basic.alldayprojectcommerce.domain.product.service;


import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.product.dto.response.GetAllProductResponse;
import jpa.basic.alldayprojectcommerce.domain.product.dto.response.GetProductResponse;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import jpa.basic.alldayprojectcommerce.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static jpa.basic.alldayprojectcommerce.domain.product.entity.QProduct.product;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryServiceImpl implements ProductQueryService{

    private final ProductRepository productRepository;
    private final JPAQueryFactory jpaQueryFactory;

    public GetProductResponse getOneProduct(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        return GetProductResponse.getProduct(product);
    }


    // 카테고리별, 판매 중

    // 전체 조회
    @Override
    public Page<GetAllProductResponse> getAll(Pageable pageable){

        List<GetAllProductResponse> responseList = jpaQueryFactory
                .select(Projections.constructor(GetAllProductResponse.class,
                        product.id,
                        product.name,
                        product.price,
                        product.status,
                        product.category
                        ))
                .from(product)
                .orderBy(product.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 데이터 개수를 세는 쿼리 객체 생성
        JPAQuery<Long> listQuery = jpaQueryFactory
                .select(product.count())
                .from(product);

        // PageableExecutionUtils : 요청한 사이즈보다 실제 사이즈 수가 작을 때 쿼리 실행 생략
        return PageableExecutionUtils.getPage(responseList, pageable, listQuery::fetchOne);
    }
}
