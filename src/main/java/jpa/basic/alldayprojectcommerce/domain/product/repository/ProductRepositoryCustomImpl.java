package jpa.basic.alldayprojectcommerce.domain.product.repository;


import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Category;
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
}