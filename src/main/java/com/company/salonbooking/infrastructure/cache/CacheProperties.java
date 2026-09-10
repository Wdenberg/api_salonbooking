package com.company.salonbooking.infrastructure.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Per-cache TTL and max-size policy (Seção 38: "Configurar TTL"). Different caches
 * have different staleness tolerances: opening hours and settings change rarely
 * (safe with a longer TTL), while service listings are read far more often and
 * benefit from a shorter TTL to limit staleness window after a price/status change
 * that isn't caught by an explicit @CacheEvict.
 */
@ConfigurationProperties(prefix = "app.cache")
public record CacheProperties(
        long businessSettingsTtlSeconds,
        long businessOpeningHoursTtlSeconds,
        long catalogServicesTtlSeconds,
        long catalogBusinessServicesTtlSeconds,
        long defaultMaxEntries
) {
    public CacheProperties {
        if (businessSettingsTtlSeconds <= 0) businessSettingsTtlSeconds = 600;      // 10 min
        if (businessOpeningHoursTtlSeconds <= 0) businessOpeningHoursTtlSeconds = 600; // 10 min
        if (catalogServicesTtlSeconds <= 0) catalogServicesTtlSeconds = 120;        // 2 min
        if (catalogBusinessServicesTtlSeconds <= 0) catalogBusinessServicesTtlSeconds = 120; // 2 min
        if (defaultMaxEntries <= 0) defaultMaxEntries = 10_000;
    }
}
