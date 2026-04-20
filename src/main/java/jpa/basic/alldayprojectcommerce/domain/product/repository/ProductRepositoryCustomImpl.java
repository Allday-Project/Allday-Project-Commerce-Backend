package jpa.basic.alldayprojectcommerce.domain.product.repository;


import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.product.dto.request.FilterProductRequest;
import jpa.basic.alldayprojectcommerce.domain.product.dto.request.SearchProductRequest;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Category;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import jpa.basic.alldayprojectcommerce.domain.product.entity.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static jpa.basic.alldayprojectcommerce.domain.product.entity.QProduct.product;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> findAllProducts(
            FilterProductRequest filterRequest,
            Pageable pageable) {
        pageable.getSort().forEach(order ->
                System.out.println("Sort Property: " + order.getProperty() + ", Direction: " + order.getDirection())
        );

        // 콘텐츠 조회 쿼리
        List<Product> content = queryFactory
                .selectFrom(product)
                .where(categoryEq(filterRequest.category()),
                        statusEq(filterRequest.status()))
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }


    // 상품 검색 기능
    @Override
    public Page<Product> searchProduct(
            SearchProductRequest searchRequest,
            Pageable pageable) {
        List<Product> content = queryFactory
                .selectFrom(product)
                .where(keywordContains(searchRequest.keyword()))
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(keywordContains(searchRequest.keyword()))
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }


    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return product.name.contains(keyword).or(product.description.contains(keyword));
    }

    private BooleanExpression categoryEq(Category category) {
        return category != null ? product.category.eq(category) : null;
    }

    private BooleanExpression statusEq(ProductStatus status) {
        return status != null ? product.status.eq(status) : null;
    }


    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        return sort.stream()
                .map(order -> {
                    Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                    String property = order.getProperty();
                    PathBuilder<Product> pathBuilder = new PathBuilder<>(Product.class, "product");
                    return new OrderSpecifier<>(direction, pathBuilder.getComparable(property, Comparable.class));
                })
                .toArray(OrderSpecifier[]::new);
    }

}