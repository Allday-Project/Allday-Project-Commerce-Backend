package jpa.basic.alldayprojectcommerce.domain.cartProduct.repository;

import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartProductRepository extends JpaRepository<OrderProduct, Long> {

    // 유저의 장바구니 목록 전체 조회
    // List<CartProduct> findAllByUserId(Long userId);

    // 유저 + 상품으로 이미 담긴 항목 찾기(중복 추가 시 수량 합산용)
    // Optional<CartProduct> findByUserIdAndProductId(Long userId, Long productId);

    // 유저의 장바구니 전체 삭제
    // void deleteAllByUserId(Long userId);

}
