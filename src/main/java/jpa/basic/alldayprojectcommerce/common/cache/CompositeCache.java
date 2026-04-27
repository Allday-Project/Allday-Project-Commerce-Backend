package jpa.basic.alldayprojectcommerce.common.cache;

import lombok.NonNull;
import org.springframework.cache.Cache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class CompositeCache implements Cache {

    private final String name;
    private final List<Cache> caches = new ArrayList<>();
    private final UpdatableCacheManager localCacheManager;

    public CompositeCache(String name, UpdatableCacheManager localCacheManager) {
        this.name = name;
        this.localCacheManager = localCacheManager;
    }

    public void addCache(Cache cache) {
        this.caches.add(cache);
    }

    @Override
    @NonNull
    public String getName() {
        return name;
    }

    @Override
    @NonNull
    public Object getNativeCache() {
        return this;
    }

    /**
     * L1 → L2 순서로 조회
     * L2 히트 시 L1에 저장 (write-back to local)
     */
    @Override
    public ValueWrapper get(@NonNull Object key) {
        for (int i = 0; i < caches.size(); i++) {
            ValueWrapper wrapper = caches.get(i).get(key);
            if (wrapper != null) {
                // L2에서 찾았으면 L1에 저장
                if (i > 0) {
                    localCacheManager.putToCache(name, key, wrapper.get());
                }
                return wrapper;
            }
        }
        return null;
    }

    @Override
    public <T> T get(@NonNull Object key, Class<T> type) {
        for (int i = 0; i < caches.size(); i++) {
            T value = caches.get(i).get(key, type);
            if (value != null) {
                if (i > 0) {
                    localCacheManager.putToCache(name, key, value);
                }
                return value;
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        ValueWrapper wrapper = get(key);
        if (wrapper != null) {
            return (T) wrapper.get();
        }
        try {
            T value = valueLoader.call();
            put(key, value);
            return value;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 모든 계층에 저장
     */
    @Override
    public void put(@NonNull Object key, Object value) {
        for (Cache cache : caches) {
            cache.put(key, value);
        }
    }

    @Override
    public void evict(@NonNull Object key) {
        for (Cache cache : caches) {
            cache.evict(key);
        }
    }

    @Override
    public void clear() {
        for (Cache cache : caches) {
            cache.clear();
        }
    }
}
