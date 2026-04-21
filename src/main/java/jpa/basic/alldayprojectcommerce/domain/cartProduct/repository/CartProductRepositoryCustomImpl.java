package jpa.basic.alldayprojectcommerce.domain.cartProduct.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jpa.basic.alldayprojectcommerce.domain.cartProduct.entity.CartProduct;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static jpa.basic.alldayprojectcommerce.domain.cartProduct.entity.QCartProduct.cartProduct;
import static jpa.basic.alldayprojectcommerce.domain.product.entity.QProduct.product;

@RequiredArgsConstructor
public class CartProductRepositoryCustomImpl implements CartProductRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CartProduct> findAllByUserIdWithCursor(Long userId, long cursorId, int size) {
        return queryFactory
                .selectFrom(cartProduct)
                .join(product).on(cartProduct.productId.eq(product.id))
                .where(
                        cartProduct.userId.eq(userId),
                        cartProduct.id.lt(cursorId) // cursorId 보다 작은 ID (최신순 페이징)
                )
                .orderBy(cartProduct.id.desc())
                .limit(size)
                .fetch();
    }

}
