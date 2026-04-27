package jpa.basic.alldayprojectcommerce.common.lock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedissonLock {

    String key();

    /**
     * 🔥 milliseconds 기준으로 변경
     */
    long waitTimeMillis() default 0;

    long leaseTimeMillis() default 3000;
}