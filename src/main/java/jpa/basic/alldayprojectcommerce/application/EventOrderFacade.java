package jpa.basic.alldayprojectcommerce.application;

import jpa.basic.alldayprojectcommerce.common.lock.service.RedisLockService;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.EventOrderResponse;
import jpa.basic.alldayprojectcommerce.domain.order.service.event.EventOrderOptimisticLockService;
import jpa.basic.alldayprojectcommerce.domain.order.service.event.EventOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventOrderFacade {

    private final EventOrderService eventOrderService;
    private final RedisLockService redisLockService;
    private final EventOrderOptimisticLockService eventOrderOptimisticLockService;


    /**
     * 락을 사용하지 않은 버전
     * @param productId 제품 ID
     * @param userId 주문자 ID
     * @return
     */
    public EventOrderResponse createEventOrderWithoutLock(Long productId, Long userId) {
        return eventOrderService.createEventOrder(productId, userId);
    }

    /**
     * Redis 분산락 - Fail Fast 전략 적용 버전
     *
     * 상품 기준으로 락을 걸어
     * 동일 상품에 대한 동시 주문을 직렬화한다.
     *
     * @param productId 제품 ID
     * @param userId 주문자 ID
     */
    public EventOrderResponse createEventOrderWithRedisLockFailFast(Long productId, Long userId) {

        // 🔥 락 키 설계 (핵심)
        String key = "lock:product:" + productId;

        // TTL (초)
        long timeoutSeconds = 5;

        return redisLockService.executeWithLockFailFast(
                key,
                timeoutSeconds,
                () -> eventOrderService.createEventOrder(productId, userId)
        );
    }

    /**
     * Redis 분산락 - Retry 전략 적용 버전
     *
     * 상품 기준으로 락을 걸어
     * 동일 상품에 대한 동시 주문을 직렬화한다.
     *
     * @param productId 제품 ID
     * @param userId 주문자 ID
     */
    public EventOrderResponse createEventOrderWithRedisLockRetry(Long productId, Long userId) {

        String key = "lock:product:" + productId;

        return redisLockService.executeWithLockRetry(
                key,
                5,
                () -> eventOrderService.createEventOrder(productId, userId)
        );
    }

    /**
     * Redis 분산락 - Blocking 전략 적용 버전
     *
     * 상품 기준으로 락을 걸어
     * 동일 상품에 대한 동시 주문을 직렬화한다.
     *
     * @param productId 제품 ID
     * @param userId 주문자 ID
     */
    public EventOrderResponse createEventOrderWithRedisLockBlocking(Long productId, Long userId) {

        String key = "lock:product:" + productId;

        return redisLockService.executeWithLockBlocking(
                key,
                5,
                () -> eventOrderService.createEventOrder(productId, userId)
        );
    }

    /**
     * JPA 낙관락 + Retry 전략 적용 버전
     */
    public EventOrderResponse createEventOrderWithOptimisticLockRetry(Long productId, Long userId) {
        return eventOrderOptimisticLockService.createEventOrderWithOptimisticLockRetry(productId, userId);
    }
}
