package jpa.basic.alldayprojectcommerce.domain.cartProduct.repository;

import jpa.basic.alldayprojectcommerce.domain.cartProduct.entity.CartProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartProductRepository extends JpaRepository<CartProduct, Long>, CartProductRepositoryCustom {

    // 유저 + 상품으로 이미 담긴 항목 찾기(중복 추가 시 수량 합산용)
    Optional<CartProduct> findByUserIdAndProductId(Long userId, Long productId);
}
