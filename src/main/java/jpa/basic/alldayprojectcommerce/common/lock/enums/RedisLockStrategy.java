package jpa.basic.alldayprojectcommerce.common.lock.enums;

public enum RedisLockStrategy {
    FAIL_FAST,
    RETRY,
    BLOCKING
}