package jpa.basic.alldayprojectcommerce.common.cache;

import org.springframework.cache.Cache;

public interface UpdatableCacheManager {
    Cache getCache(String name);
    void putToCache(String name, Object key, Object value);
}
