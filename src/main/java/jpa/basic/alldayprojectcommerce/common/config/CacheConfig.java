package jpa.basic.alldayprojectcommerce.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@EnableCaching
@Configuration
public class CacheConfig {


    // 동시성 맵 캐시 관리자
    @Bean
    public CacheManager cacheManager(){
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(60, TimeUnit.SECONDS)
        );

        // null 값이 캐싱되지 않도록 설정
        // 하지만 null 값 반환이 아닌 빈 페이지를 반환하기에 사용하지 않는다.
//        cacheManager.setAllowNullValues(true);
        return cacheManager;
    }
}