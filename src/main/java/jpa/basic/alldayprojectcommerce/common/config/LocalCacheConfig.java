package jpa.basic.alldayprojectcommerce.common.config;


import com.github.benmanes.caffeine.cache.Caffeine;
import jpa.basic.alldayprojectcommerce.common.cache.CacheName;
import jpa.basic.alldayprojectcommerce.common.cache.CacheType;
import jpa.basic.alldayprojectcommerce.common.cache.LocalCacheManager;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@EnableCaching
@Configuration
public class LocalCacheConfig {
    // 로컬 캐시 설정 클래스

    // 로컬 캐시에 담을 수 있는 무게 한도 : 10MB
    private static final int MAX_WEIGHT = 10_000_000;


    @Bean
    // 로컬 캐시들을 관리할 객체를 만들어서 반환하는 메서드
    public LocalCacheManager localcacheManager() {
        List<Cache> caches = CacheName.entries().stream() // CacheName enum의 전체 목록 리스트를 가져옴
                .filter(g -> g.getCacheType() != CacheType.GLOBAL) // 설정된 타입이 GLOBAL(Redis만 사용하는 타입)인 것들은 제외
                .map(g -> {
                    Caffeine<Object, Object> builder = Caffeine.newBuilder() // 세부 옵션 설정 빌더 객체
                            .expireAfterWrite(g.getTtl().toSeconds(), TimeUnit.SECONDS) // TTL 시간이 지나면 자동 만료
//                            .maximumWeight(MAX_WEIGHT) // 캐시 무게는 위에 설정한 10MB를 못 넘김
//                            .weigher((key, value) -> estimateWeight(value)); // 캐시에 들어오는 각 데이터(value)의 무게를 어떻게 측정할 것인가
                            .maximumSize(g.getMaximumSize());
                    return (Cache) new CaffeineCache(g.getCacheName(), builder.build());
                })
                .collect(Collectors.toList());
        return new LocalCacheManager(caches);
    }

//    // 무게 측정 메서드 : value를 넘겨 받아서 데이터 무게를 int로 반환
//    private int estimateWeight(Object value) {
//        if (value == null) // 캐시가 비어있다면 최소 무게 1을 반환(캐시 공간을 차지하고 있다는 걸 표시)
//            return 1;
//        return value.toString() // 문자열로 변환된 데이터를 UTF-8 인코딩 기준의 바이트(Byte) 배열로 쪼갬
//                .getBytes(StandardCharsets.UTF_8)
//                .length;
//    }

    // 결론 : 총합(MAX_WEIGH)를 넘어가면 오래된 캐시부터 삭제
}
