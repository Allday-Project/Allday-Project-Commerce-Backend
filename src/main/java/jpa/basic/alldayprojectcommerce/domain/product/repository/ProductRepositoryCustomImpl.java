package jpa.basic.alldayprojectcommerce.domain.product.repository;


import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
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
    public Page<Product> findAllProducts(String category, String keyword, Pageable pageable) {

        // 1. 콘텐츠 조회 쿼리
        JPAQuery<Product> query = queryFactory
                .selectFrom(product)
                .where(categoryEq(category), nameContains(keyword));

        // 정렬 조건 적용
        if (pageable.getSort().isSorted()) {
            pageable.getSort().forEach(order -> {
                com.querydsl.core.types.Order direction = order.isAscending() ? com.querydsl.core.types.Order.ASC : com.querydsl.core.types.Order.DESC;
                switch (order.getProperty()) {
                    case "price":
                        query.orderBy(new com.querydsl.core.types.OrderSpecifier<>(direction, product.price));
                        break;
                    case "name":
                        query.orderBy(new com.querydsl.core.types.OrderSpecifier<>(direction, product.name));
                        break;
                    case "id":
                    default:
                        query.orderBy(new com.querydsl.core.types.OrderSpecifier<>(direction, product.id));
                        break;
                }
            });
        } else {
            query.orderBy(product.id.desc());
        }

        List<Product> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .where(categoryEq(category), nameContains(keyword));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression nameContains(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return product.name.containsIgnoreCase(keyword);
    }

    private BooleanExpression categoryEq(String category) {
        if (category == null || category.isEmpty() || "ALL".equalsIgnoreCase(category)) {
            return null;
        }

        try {
            Category categoryEnum = Category.valueOf(category.toUpperCase());
            return product.category.eq(categoryEnum);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


    // 상품 검색 기능
    @Override
    public Page<Product> searchProduct(
            SearchProductRequest searchRequest,
            Pageable pageable) {
        List<Product> content = queryFactory
                .selectFrom(product)
                .where(keywordContainsV1(searchRequest.keyword()))
                .orderBy(getOrderSpecifiersV1(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(keywordContainsV1(searchRequest.keyword()))
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }




    // 키워드에 빈 값이 들어갔을 때 에러
    private BooleanExpression keywordContainsV1(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return product.name.containsIgnoreCase(keyword).or(product.description.contains(keyword));
    }

    // 카테고리에 빈 값이 들어갔을 때 에러
    private BooleanExpression categoryEqV1(Category category) {
        return category != null ? product.category.eq(category) : null;
    }

    // 상태에 빈 값이 들어갔을 때 에러
    private BooleanExpression statusEqV1(ProductStatus status) {
        return status != null ? product.status.eq(status) : null;
    }

    // 정렬 조건을 동적으로 생성하는 메서드
    private OrderSpecifier<?>[] getOrderSpecifiersV1(Sort sort) {
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