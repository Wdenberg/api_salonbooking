package com.company.salonbooking.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

import static com.company.salonbooking.infrastructure.cache.CacheNames.*;

/**
 * Local Caffeine-backed cache (still Seção 7's "começar simples, preferencialmente
 * com cache local"), now with real per-entry TTL and a bounded max size — unlike the
 * ConcurrentMapCacheManager used in Fases 3/5, which never expired anything and could
 * grow unbounded.
 *
 * Migrating to Redis later (Seção 7) means replacing only this @Configuration class
 * with one that builds a RedisCacheManager instead — every @Cacheable/@CacheEvict
 * annotation across business/catalog use cases stays exactly as written.
 *
 * Never used for: appointments or real-time availability (Seção 37) — no cache name
 * exists for those, by design, so there is no annotation anywhere in scheduling that
 * could accidentally introduce staleness into a double-booking-sensitive read path.
 */
@Configuration
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(CacheProperties properties) {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                buildCache(BUSINESS_SETTINGS, properties.businessSettingsTtlSeconds(), properties.defaultMaxEntries()),
                buildCache(BUSINESS_OPENING_HOURS, properties.businessOpeningHoursTtlSeconds(), properties.defaultMaxEntries()),
                buildCache(CATALOG_SERVICES, properties.catalogServicesTtlSeconds(), properties.defaultMaxEntries()),
                buildCache(CATALOG_BUSINESS_SERVICES, properties.catalogBusinessServicesTtlSeconds(), properties.defaultMaxEntries())
        ));
        manager.initializeCaches();
        return manager;
    }

    private CaffeineCache buildCache(String name, long ttlSeconds, long maxEntries) {
        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(ttlSeconds))
                .maximumSize(maxEntries)
                .recordStats() // exposed via Actuator/Micrometer in Fase 14
                .build();
        return new CaffeineCache(name, nativeCache);
    }
}