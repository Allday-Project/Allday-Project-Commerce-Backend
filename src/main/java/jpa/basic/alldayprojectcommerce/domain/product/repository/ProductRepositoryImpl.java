package jpa.basic.alldayprojectcommerce.domain.product.repository;


import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static jpa.basic.alldayprojectcommerce.domain.product.entity.QProduct.product;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustomImpl {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> findAllProducts(Pageable pageable) {

        // 1. 콘텐츠 조회 쿼리
        List<Product> content = queryFactory
                .selectFrom(product)
                .orderBy(product.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}