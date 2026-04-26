package jpa.basic.alldayprojectcommerce.common.lock.annotation;

import jpa.basic.alldayprojectcommerce.common.lock.enums.RedisLockStrategy;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisLock {

    /**
     * SpEL 기반 락 키
     * 예: "'lock:product:' + #productId"
     */
    String key();

    /**
     * Redis 락 TTL
     */
    long timeoutSeconds() default 5;

    /**
     * 락 획득 전략
     */
    RedisLockStrategy strategy() default RedisLockStrategy.RETRY;
}
