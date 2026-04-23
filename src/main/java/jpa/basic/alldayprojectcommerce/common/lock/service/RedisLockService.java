package jpa.basic.alldayprojectcommerce.common.lock.service;

import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.common.lock.repository.RedisLockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * 실패 시 즉시 예외
 * 비즈니스 로직 실행
 * finally에서 락 해제
 *
 * 락 흐름 제어 담당
 * key/value/ttl/예외 처리
 * 상위 비즈니스에서 쓰기 쉬움
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLockService {

    private final RedisLockRepository redisLockRepository;

    /**
     * Redis 분산락을 획득한 뒤 비즈니스 로직 실행
     *
     * 현재 단계에서는 Fail Fast 전략을 사용한다.
     * 즉, 락 획득에 실패하면 재시도하지 않고 바로 예외를 던진다.
     *
     * @param key 락 키 (예: lock:product:4)
     * @param timeoutSeconds 락 유지 시간
     * @param supplier 락 안에서 실행할 비즈니스 로직
     * @return supplier 실행 결과
     * @param <T> 반환 타입
     */
    public <T> T executeWithLockFailFast(String key, long timeoutSeconds, Supplier<T> supplier) {
        String lockValue = UUID.randomUUID().toString();

        boolean locked = redisLockRepository.tryLock(key, lockValue, timeoutSeconds);

        if (!locked) {
            log.info("[RedisLock] 락 획득 실패 key={}", key);
            throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILED);
        }

        log.info("[RedisLock] 락 획득 성공 key={}, value={}", key, lockValue);

        try {
            // 목표하는 메서드 수행
            return supplier.get();
        } finally {
            // 락 해제
            boolean unlocked = redisLockRepository.unlock(key, lockValue);

            if (unlocked) {
                log.info("[RedisLock] 락 해제 성공 key={}, value={}", key, lockValue);
            } else {
                log.warn("[RedisLock] 락 해제 실패 또는 이미 해제됨 key={}, value={}", key, lockValue);
            }
        }
    }


    public <T> T executeWithLockRetry(String key, long timeoutSeconds, Supplier<T> supplier) {

        String lockValue = UUID.randomUUID().toString();

        // 최대 재시도 횟수
        int maxRetryCount = 10;
        // 재시도 간격(ms)
        long retryIntervalMillis = 100L;    // 최대 대기 시간은 대략 (20-1) * 100ms = 1.9초
        // TODO : 10개보다 적게 성공한다면
        // 1. 재시도 횟수 증가
        // 2. 대기 시간 약간 증가

        for (int attempt = 1; attempt <= maxRetryCount; attempt++) {

            boolean locked = redisLockRepository.tryLock(key, lockValue, timeoutSeconds);

            if (locked) {
                log.info("[RedisLock-Retry] 락 획득 성공 key={}, attempt={}", key, attempt);

                try {
                    return supplier.get();
                } finally {
                    redisLockRepository.unlock(key, lockValue);
                }
            }

            log.info("[RedisLock-Retry] 락 획득 실패 key={}, attempt={}", key, attempt);

            if (attempt == maxRetryCount) {
                break;
            }

            try {
                Thread.sleep(retryIntervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILED);
            }
        }

        throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILED);
    }

    public <T> T executeWithLockBlocking(String key, long timeoutSeconds, Supplier<T> supplier) {

        String lockValue = UUID.randomUUID().toString();

        long waitStartTime = System.currentTimeMillis();

        long maxWaitMillis = 5000; // 🔥 최대 5초만 기다림
        long retryIntervalMillis = 50;

        while (true) {

            boolean locked = redisLockRepository.tryLock(key, lockValue, timeoutSeconds);

            if (locked) {
                log.info("[RedisLock-Blocking] 락 획득 성공 key={}", key);

                try {
                    return supplier.get();
                } finally {
                    redisLockRepository.unlock(key, lockValue);
                }
            }

            // 🔥 최대 대기 시간 초과
            if (System.currentTimeMillis() - waitStartTime > maxWaitMillis) {
                log.warn("[RedisLock-Blocking] 락 획득 타임아웃 key={}", key);
                throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILED);
            }

            try {
                Thread.sleep(retryIntervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILED);
            }
        }
    }

}
