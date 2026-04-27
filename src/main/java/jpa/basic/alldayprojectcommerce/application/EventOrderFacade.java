package jpa.basic.alldayprojectcommerce.application;

import jpa.basic.alldayprojectcommerce.common.lock.annotation.RedisLock;
import jpa.basic.alldayprojectcommerce.common.lock.annotation.RedissonLock;
import jpa.basic.alldayprojectcommerce.common.lock.enums.RedisLockStrategy;
import jpa.basic.alldayprojectcommerce.common.lock.service.RedisLockService;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.EventOrderResponse;
import jpa.basic.alldayprojectcommerce.domain.order.service.event.EventOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventOrderFacade {

    private final EventOrderService eventOrderService;
    private final RedisLockService redisLockService;

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
     *  비관적 락 사용
     * @param productId
     * @param userId
     * @return
     */
    public EventOrderResponse createEventOrderWithPessimisticLock(Long productId, Long userId) {
        return eventOrderService.createEventOrderWithPessimisticLock(productId, userId);
    }

    /**
     * Redis Lettuce 분산락 - Fail Fast 전략 적용 버전
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
     * Redis Lettuce 분산락 - Retry 전략 적용 버전
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
     * Redis Lettuce 분산락 - Blocking 전략 적용 버전
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
     * Redis Lettuce 분산락 - AOP FailFast 전략 적용 버전
     */
    @RedisLock(
            key = "'lock:product:' + #productId",
            timeoutSeconds = 5,
            strategy = RedisLockStrategy.FAIL_FAST
    )
    public EventOrderResponse createEventOrderWithRedisLockAopFailFast(Long productId, Long userId) {
        return eventOrderService.createEventOrder(productId, userId);
    }

    /**
     * Redis Lettuce 분산락 - AOP Retry 전략 적용 버전
     */
    @RedisLock(
            key = "'lock:product:' + #productId",
            timeoutSeconds = 5,
            strategy = RedisLockStrategy.RETRY
    )
    public EventOrderResponse createEventOrderWithRedisLockAopRetry(Long productId, Long userId) {
        return eventOrderService.createEventOrder(productId, userId);
    }

    /**
     * Redis Lettuce 분산락 - AOP Blocking 전략 적용 버전
     */
    @RedisLock(
            key = "'lock:product:' + #productId",
            timeoutSeconds = 5,
            strategy = RedisLockStrategy.BLOCKING
    )
    public EventOrderResponse createEventOrderWithRedisLockAopBlocking(Long productId, Long userId) {
        return eventOrderService.createEventOrder(productId, userId);
    }


    /**
     * Redisson 분산락 - FailFast
     *
     * 거의 기다리지 않고 락 획득 실패 시 바로 실패.
     * 성능 비교용으로만 사용.
     */
    @RedissonLock(
            key = "'lock:product:' + #productId",
            waitTimeMillis = 0,
            leaseTimeMillis = 3000 // TTL
    )
    public EventOrderResponse createEventOrderWithRedissonLockAopFailFast(Long productId, Long userId) {
        return eventOrderService.createEventOrder(productId, userId);
    }

    /**
     * Redisson 분산락 - Retry + TTL
     *
     * 짧게 실패하지 않고 어느 정도 대기.
     */
    @RedissonLock(
            key = "'lock:product:' + #productId",
            waitTimeMillis = 5000,
            leaseTimeMillis = 3000
    )
    public EventOrderResponse createEventOrderWithRedissonLockAopRetry(Long productId, Long userId) {
        return eventOrderService.createEventOrder(productId, userId);
    }


    /**
     * Redisson 분산락 - Blocking + TTL
     *
     * 재고 100개 완판을 목표로 충분히 기다림.
     */
    @RedissonLock(
            key = "'lock:product:' + #productId",
            waitTimeMillis = 10000,
            leaseTimeMillis = 5000
    )
    public EventOrderResponse createEventOrderWithRedissonLockAopBlocking(Long productId, Long userId) {
        return eventOrderService.createEventOrder(productId, userId);
    }



    /**
     * Redisson 분산락 - Retry + Watchdog
     *
     * 락 획득은 최대 5초 대기,
     * 락 획득 후에는 Watchdog이 TTL 자동 연장.
     */
    @RedissonLock(
            key = "'lock:product:' + #productId",
            waitTimeMillis = 5000,
            leaseTimeMillis = -1
    )
    public EventOrderResponse createEventOrderWithRedissonLockAopRetryWatchdog(Long productId, Long userId) {
        return eventOrderService.createEventOrder(productId, userId);
    }

    /**
     * Redisson 분산락 - Blocking + Watchdog
     *
     * 현재 테스트에서 가장 유력한 후보.
     * 재고 100개를 최대한 정상 소진시키는 목적.
     */
    @RedissonLock(
            key = "'lock:product:' + #productId",
            waitTimeMillis = 10000,
            leaseTimeMillis = -1
    )
    public EventOrderResponse createEventOrderWithRedissonLockAopBlockingWatchdog(Long productId, Long userId) {
        return eventOrderService.createEventOrder(productId, userId);
    }

    /**
     * Redisson 분산락 - Blocking + Watchdog + 비관락
     *
     * 현재 테스트에서 가장 유력한 후보.
     * 재고 100개를 최대한 정상 소진시키는 목적.
     */
    @RedissonLock(
            key = "'lock:product:' + #productId",
            waitTimeMillis = 10000,
            leaseTimeMillis = -1
    )
    public EventOrderResponse createEventOrderWithRedissonLockAopBlockingWatchdogWithPessimisticLock(Long productId, Long userId) {
        return eventOrderService.createEventOrderWithPessimisticLock(productId, userId);
    }


}
