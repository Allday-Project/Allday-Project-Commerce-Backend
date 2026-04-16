package jpa.basic.alldayprojectcommerce.domain.order.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static jpa.basic.alldayprojectcommerce.domain.order.entity.QOrder.order;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Order> findByUserIdWithCursor(Long userId, Long cursorId, int size) {
        return queryFactory
                .selectFrom(order)
                .where(
                        userIdEq(userId),
                        cursorIdLt(cursorId)
                )
                .orderBy(order.id.desc())
                .limit(size + 1L)
                .fetch();
    }

    // 유저 ID 조건
    private BooleanExpression userIdEq(Long userId) {
        return (userId != null) ? order.userId.eq(userId) : null;
    }

    // 커서 조건 - 첫 요청은 가장 최신
    private BooleanExpression cursorIdLt(Long cursorId) {
        return (cursorId != null) ? order.id.lt(cursorId) : null;
    }
}
