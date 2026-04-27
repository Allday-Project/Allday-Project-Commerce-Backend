package jpa.basic.alldayprojectcommerce.common.lock.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Collections;

/**
 * Redis 명령 담당
 * setIfAbsent, Lua Script 해제
 * 저수준 인프라 역할
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class RedisLockRepository {

    private final StringRedisTemplate redisTemplate;

    /**
     * Redis 락 획득 시도
     *
     * setIfAbsent == SET NX
     * TTL을 함께 걸어 서버 장애 시 락이 영구 점유되지 않도록 한다.
     *
     * @param key 락 키 (예: lock:product:4)
     * @param value 락 소유자 식별값 (UUID)
     * @param timeoutSeconds 락 유지 시간
     * @return 락 획득 성공 여부
     */
    public boolean tryLock(String key, String value, long timeoutSeconds) {
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(key, value, Duration.ofSeconds(timeoutSeconds));

        return Boolean.TRUE.equals(result);
    }

    /**
     * Redis 락 해제
     *
     * 본인이 획득한 락만 해제해야 하므로
     * value(UUID)를 비교한 뒤 동일할 때만 delete 한다.
     * 이 과정을 Lua Script로 원자적으로 수행한다.
     *
     * @param key 락 키
     * @param value 락 소유자 식별값 (UUID)
     * @return 실제 삭제 여부
     */
    public boolean unlock(String key, String value) {
        String script = """
                if redis.call('get', KEYS[1]) == ARGV[1] then
                    return redis.call('del', KEYS[1])
                else
                    return 0
                end
                """;

        Long result = redisTemplate.execute(
                new DefaultRedisScript<>(script, Long.class),
                Collections.singletonList(key),
                value
        );
        log.info("[RedisLock] unlock 시도 key={}, value={}", key, value);

        return result != null && result == 1L;
    }
}