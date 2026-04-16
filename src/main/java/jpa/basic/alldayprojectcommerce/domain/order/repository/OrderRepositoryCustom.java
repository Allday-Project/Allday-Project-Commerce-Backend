package jpa.basic.alldayprojectcommerce.domain.order.repository;

import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;

import java.util.List;

public interface OrderRepositoryCustom {

    /**
     * 커서 기반 무한 스크롤 주문 목록 조회
     *
     * @param userId    : 조회할 유저 ID
     * @param cursorId  : 마지막으로 읽은 주문 ID (첫 요청은 null)
     * @param size      : 한 번에 가져올 개수
     * @return          : hasNext 판별을 위해 size + 1 반환
     */
    List<Order> findByUserIdWithCursor(Long userId, Long cursorId, int size);
}
