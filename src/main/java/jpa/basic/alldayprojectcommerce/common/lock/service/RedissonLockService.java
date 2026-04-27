package jpa.basic.alldayprojectcommerce.common.lock.service;

import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedissonLockService {

    private final RedissonClient redissonClient;

    public <T> T executeWithLock(
            String key,
            long waitTimeMillis,
            long leaseTimeMillis,
            Supplier<T> supplier
    ) {

        /**
         * RLock = Redisson의 분산락 객체
         *
         * - key 기준으로 락 생성
         * - 내부적으로 Redis 사용
         */
        RLock lock = redissonClient.getLock(key);
        boolean locked = false;

        try {
            /**
             * tryLock
             *
             * waitTimeSeconds: 락을 기다리는 시간 (Blocking 느낌)
             * leaseTimeSeconds: 락 유지 시간
             *
             * Lettuce FailFast 전략 -> waitTime = 0
             * Retry -> waitTime > 0 짧게
             * Blocking -> waitTime을 크게
             */

            if (leaseTimeMillis < 0) {
                /**
                 * 🔥 watchdog 모드
                 *
                 * - TTL을 자동으로 계속 연장
                 * - 비즈니스 로직이 길어도 안전
                 *
                 * Lettuce에서는 직접 구현 불가능
                 */
                locked = lock.tryLock(waitTimeMillis, TimeUnit.MILLISECONDS);
            } else {
                /**
                 * 🔥 일반 TTL 방식
                 *
                 * - leaseTime 지나면 자동 unlock
                 * - Lettuce의 TTL과 동일 개념
                 */
                locked = lock.tryLock(waitTimeMillis, leaseTimeMillis, TimeUnit.MILLISECONDS);
            }

            /**
             * 🔥 락 획득 실패
             *
             * - waitTime 동안 기다렸는데도 실패하면 여기로 옴
             */
            if (!locked) {
                log.info("[RedissonLock] 락 획득 실패 key={}", key);
                throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILED);
            }

            log.info("[RedissonLock] 락 획득 성공 key={}", key);

            /**
             * 🔥 실제 비즈니스 로직 실행
             */
            return supplier.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILED);

        } finally {
            /**
             * 🔥 매우 중요
             *
             * lock.isHeldByCurrentThread() → 내가 잡은 락인지 확인
             *
             * Lettuce에서는:
             * UUID + Lua Script로 직접 구현했지만
             *
             * Redisson에서는:
             * 내부적으로 자동 관리됨
             */
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("[RedissonLock] 락 해제 성공 key={}", key);
            }
        }
    }
}