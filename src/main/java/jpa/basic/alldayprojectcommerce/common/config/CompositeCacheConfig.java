package jpa.basic.alldayprojectcommerce.common.config;

import jpa.basic.alldayprojectcommerce.common.cache.CompositeCacheCacheManager;
import jpa.basic.alldayprojectcommerce.common.cache.LocalCacheManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
@EnableCaching
public class CompositeCacheConfig {

    @Primary
    @Bean
    public CacheManager cacheManager(
            LocalCacheManager local,
            @Qualifier("redisCacheManager") CacheManager redis
    ) {
        return new CompositeCacheCacheManager(
                List.of(local, redis),
                local
        );
    }
}
