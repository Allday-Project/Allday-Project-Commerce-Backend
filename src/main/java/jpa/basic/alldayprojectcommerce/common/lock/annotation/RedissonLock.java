package jpa.basic.alldayprojectcommerce.common.lock.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedissonLock {

    /**
     * SpEL 기반 락 키
     * 예: "'lock:product:' + #productId"
     */
    String key();

    /**
     * 락 획득을 기다릴 최대 시간
     */
    long waitTimeSeconds() default 3;

    /**
     * 락 점유 시간
     *
     * - 양수: 지정 시간 후 자동 해제
     * - -1: leaseTime 없이 watchdog 사용
     */
    long leaseTimeSeconds() default 5;
}