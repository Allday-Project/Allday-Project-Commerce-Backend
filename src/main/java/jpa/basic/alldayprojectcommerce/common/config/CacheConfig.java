package jpa.basic.alldayprojectcommerce.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class CacheConfig {

    // 동시성 맵 캐시 관리자
    @Bean
    public CacheManager caffeineCacheManager(){
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "productSearchCache",
                "top5Keywords"
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .recordStats()
        );

        // null 값이 캐싱되지 않도록 설정
        // 하지만 null 값 반환이 아닌 빈 리스트를 반환하기에 사용하지 않는다.
//        cacheManager.setAllowNullValues(true);
        return cacheManager;
    }

//    public void logCacheStatus(CacheManager cacheManager) {
//        CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache("top5Keywords");
//
//        if (caffeineCache == null) {
//            log.warn("[캐시 통계] top5Keywords 캐시를 찾을 수 없습니다.");
//            return;
//        }
//
//        Cache<Object, Object> cache = caffeineCache.getNativeCache();
//
//        log.info("[캐시 통계] hit: {}, miss: {}, hitRate: {}",
//                cache.stats().hitCount(), cache.stats().missCount(), cache.stats().hitRate());
//    }
}