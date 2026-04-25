package jpa.basic.alldayprojectcommerce.common.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CompositeCacheCacheManager implements CacheManager {

    private final List<CacheManager> cacheManagers;
    private final UpdatableCacheManager localCacheManager;

    public CompositeCacheCacheManager(List<CacheManager> cacheManagers, UpdatableCacheManager localCacheManager) {
        this.cacheManagers = cacheManagers;
        this.localCacheManager = localCacheManager;
    }

    /*
     * L1(Caffeine) → L2(Redis) 순서로 캐시 조회
     * - L1에 있으면 바로 반환
     * - L2에 있으면 L1에 저장 후 반환
     * - 둘 다 없으면 null → DB 조회 후 @Cacheable이 자동 저장
     */
    @Override
    public Cache getCache(String name) {
        CompositeCache compositeCache = null;

        for (CacheManager manager : cacheManagers) {
            Cache cache = manager.getCache(name);
            if (cache != null) {
                if (compositeCache == null) {
                    compositeCache = new CompositeCache(name, localCacheManager);
                }
                compositeCache.addCache(cache);
            }
        }

        return compositeCache;
    }

    @Override
    public Collection<String> getCacheNames() {
        Set<String> names = new LinkedHashSet<>();
        for (CacheManager manager : cacheManagers) {
            names.addAll(manager.getCacheNames());
        }
        return names;
    }
}
