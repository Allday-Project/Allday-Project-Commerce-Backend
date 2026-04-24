package jpa.basic.alldayprojectcommerce.common.lock.aspect;

import jpa.basic.alldayprojectcommerce.common.lock.annotation.RedisLock;
import jpa.basic.alldayprojectcommerce.common.lock.enums.RedisLockStrategy;
import jpa.basic.alldayprojectcommerce.common.lock.service.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * RedisLockAspect
 *
 * 역할:
 * @RedisLock 애노테이션이 붙은 메서드를 가로채서
 * Redis 분산락 획득 → 실제 메서드 실행 → Redis 분산락 해제 흐름을 적용한다.
 *
 * 즉, 비즈니스 코드에서 직접 redisLockService.executeWithLock...()을 호출하지 않아도
 * 애노테이션만 붙이면 락 처리가 자동으로 적용된다.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisLockAspect {

    /**
     * 이 Aspect는 "언제 어떤 key로 어떤 전략의 락을 걸지" 결정하고,
     * 실제 락 처리는 RedisLockService에게 위임한다.
     */
    private final RedisLockService redisLockService;

    /**
     * SpEL 표현식을 해석하기 위한 파서
     *
     * 예:
     * "'lock:product:' + #productId"
     *
     * productId가 4라면
     * "lock:product:4" 로 변환한다.
     */
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * @RedisLock 애노테이션이 붙은 메서드를 감싸는 Around Advice
     *
     * @Around("@annotation(redisLock)")
     * - @RedisLock이 붙은 메서드가 호출되면 이 메서드가 먼저 실행된다.
     */
    @Around("@annotation(redisLock)")
    public Object around(ProceedingJoinPoint joinPoint, RedisLock redisLock) {

        /**
         * redisLock 파라미터로 어노테이션에 적은 값을 가져온다.
         * key = "'lock:product:' + #productId"
         * timeoutSeconds = 5
         * strategy = RETRY
         */

        // 1. 애노테이션에 작성한 key 표현식을 실제 Redis Lock Key로 변환
        String key = parseKey(joinPoint, redisLock.key());
        // 2. 애노테이션에서 설정한 락 전략과 TTL 값을 가져온다.
        RedisLockStrategy strategy = redisLock.strategy();
        long timeoutSeconds = redisLock.timeoutSeconds();
        log.info("[RedisLock-AOP] key={}, strategy={}", key, strategy);

        return switch (strategy) {
            case FAIL_FAST -> redisLockService.executeWithLockFailFast(
                    key,
                    timeoutSeconds,
                    /**
                     * 핵심:
                     * () -> proceed(joinPoint)
                     *
                     * 이 람다가 바로 "락을 잡은 뒤 실행할 실제 비즈니스 로직"이다.
                     */
                    () -> proceed(joinPoint)
            );

            case RETRY -> redisLockService.executeWithLockRetry(
                    key,
                    timeoutSeconds,
                    () -> proceed(joinPoint)
            );

            case BLOCKING -> redisLockService.executeWithLockBlocking(
                    key,
                    timeoutSeconds,
                    () -> proceed(joinPoint)
            );
        };
    }

    private Object proceed(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed();
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            /*
             * joinPoint.proceed()는 Throwable을 던질 수 있다.
             *
             * 하지만 Supplier는 checked exception을 던질 수 없으므로
             * checked exception은 RuntimeException으로 감싸서 던진다.
             */
            throw new RuntimeException(e);
        }
    }

    private String parseKey(ProceedingJoinPoint joinPoint, String keyExpression) {
        /*
         * MethodSignature:
         * 현재 AOP가 가로챈 메서드의 시그니처 정보
         *
         * 여기서 파라미터 이름을 얻을 수 있다.
         *
         * 예:
         * createEventOrderWithRedisLockAopRetry(Long productId, Long userId)
         *
         * parameterNames = ["productId", "userId"]
         * args = [4L, 1L]
         */
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();


        /*
         * SpEL에서 사용할 변수 저장소
         *
         * context에 productId=4, userId=1 같은 변수를 넣어두면
         * SpEL 표현식에서 #productId, #userId로 사용할 수 있다.
         */
        StandardEvaluationContext context = new StandardEvaluationContext();

        for (int i = 0; i < parameterNames.length; i++) {
            /*
             * 파라미터 이름과 실제 인자값을 매칭해서 context에 등록한다.
             *
             * 예:
             * parameterNames[0] = "productId"
             * args[0] = 4L
             *
             * context.setVariable("productId", 4L)
             */
            context.setVariable(parameterNames[i], args[i]);
        }

        /*
         * SpEL 표현식을 실제 문자열로 평가한다.
         *
         * 예:
         * "'lock:product:' + #productId"
         * → "lock:product:4"
         */
        return parser.parseExpression(keyExpression).getValue(context, String.class);
    }
}