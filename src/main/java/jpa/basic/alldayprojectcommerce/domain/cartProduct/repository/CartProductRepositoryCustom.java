package jpa.basic.alldayprojectcommerce.domain.cartProduct.repository;

import jpa.basic.alldayprojectcommerce.domain.cartProduct.entity.CartProduct;
import jpa.basic.alldayprojectcommerce.domain.cartProduct.entity.CartProduct;
import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;

import java.util.List;

public interface CartProductRepositoryCustom {

    // 커서 기반 유저의 장바구니 목록 전체 조회
    List<CartProduct> findAllByUserIdWithCursor(Long loginId, long cursor, int size);

}
