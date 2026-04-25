package jpa.basic.alldayprojectcommerce.common.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleCacheManager;

import java.util.Collection;

public class LocalCacheManager extends SimpleCacheManager implements UpdatableCacheManager {

    public LocalCacheManager(Collection<? extends Cache> caches) {
        setCaches(caches);
        afterPropertiesSet();
    }

    @Override
    public void putToCache(String name, Object key, Object value) {
        Cache cache = getCache(name);
        if (cache != null) {
            cache.put(key, value);
        }
    }
}
