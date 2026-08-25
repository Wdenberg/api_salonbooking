package com.company.salonbooking.infrastructure.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Local in-memory cache for now (Seção 7). Swapping to Redis later means replacing
 * only this bean — @Cacheable/@CacheEvict annotations at the use-case level don't change.
 */
@Configuration
public class CacheConfig {

    public static final String BUSINESS_SETTINGS = "business-settings";
    public static final String BUSINESS_OPENING_HOURS = "business-opening-hours";
    public static final String CATALOG_SERVICES = "catalog-services";
    public static final String CATALOG_BUSINESS_SERVICES = "catalog-business-services";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                BUSINESS_SETTINGS, BUSINESS_OPENING_HOURS, CATALOG_SERVICES, CATALOG_BUSINESS_SERVICES);
    }
}