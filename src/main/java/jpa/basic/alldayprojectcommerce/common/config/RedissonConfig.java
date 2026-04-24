package jpa.basic.alldayprojectcommerce.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    /**
     *  RedissonClient = Redis 분산락 핵심 객체
     *
     * - Lettuce에서는 StringRedisTemplate으로 직접 SET NX 호출
     * - Redisson에서는 RedissonClient 하나로 Lock, Queue, Semaphore 등 다 처리함
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(RedisProperties redisProperties) {
        Config config = new Config();
        /**
         * Redis 주소 설정
         *
         * - Spring Redis 설정(application.yml)에 있는 host, port 가져옴
         * - redis:// prefix 꼭 필요
         */
        String address = "redis://" + redisProperties.getHost() + ":" + redisProperties.getPort();

        config.useSingleServer()
                .setAddress(address);

        /**
         * RedissonClient 생성
         *
         * - 내부적으로 Netty 기반으로 Redis와 통신
         * - RLock 등 분산락 기능 제공
         */
        return Redisson.create(config);
    }
}