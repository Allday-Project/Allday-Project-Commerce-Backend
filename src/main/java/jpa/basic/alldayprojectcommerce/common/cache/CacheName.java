package jpa.basic.alldayprojectcommerce.common.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum CacheName {
    // 캐시 이름, TTL, 캐시타입을 관리하는 이넘

    PRODUCT_SEARCH("productSearch", Duration.ofMinutes(5), CacheType.COMPOSITE),
    PRODUCT_DETAIL("productDetail", Duration.ofMinutes(5), CacheType.COMPOSITE);

    private final String cacheName;
    private final Duration ttl;
    private final CacheType cacheType;

    // CacheName enum에 정의해둔 모든 상수(캐시 설정값들)를 List 형태로 묶어서 반환해주는 역할
    public static List<CacheName> entries() {
        return Arrays.asList(values());
    }
}
