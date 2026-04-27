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

        RLock lock = redissonClient.getLock(key);
        boolean locked = false;

        try {

            if (leaseTimeMillis < 0) {
                /**
                 * 🔥 watchdog 모드
                 */
                locked = lock.tryLock(waitTimeMillis, TimeUnit.MILLISECONDS);
            } else {
                /**
                 * 🔥 일반 TTL 방식
                 */
                locked = lock.tryLock(waitTimeMillis, leaseTimeMillis, TimeUnit.MILLISECONDS);
            }

            if (!locked) {
                log.info("[RedissonLock] 락 획득 실패 key={}", key);
                throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILED);
            }

            log.info("[RedissonLock] 락 획득 성공 key={}", key);

            return supplier.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILED);

        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("[RedissonLock] 락 해제 성공 key={}", key);
            }
        }
    }
}