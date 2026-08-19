package com.company.salonbooking.infrastructure.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Local in-memory cache for now (Seção 7). The abstraction (CacheManager bean +
 * @Cacheable/@CacheEvict annotations at the use-case level) allows swapping to
 * Redis later without touching business rules — only this bean changes.
 */
@Configuration
public class CacheConfig {

    public static final String BUSINESS_SETTINGS = "business-settings";
    public static final String BUSINESS_OPENING_HOURS = "business-opening-hours";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(BUSINESS_SETTINGS, BUSINESS_OPENING_HOURS);
    }
}