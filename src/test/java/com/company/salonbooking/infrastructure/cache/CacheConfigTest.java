package com.company.salonbooking.infrastructure.cache;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigTest {

    @Test
    void deveCriarTodosOsCachesConfigurados() {
        CacheProperties properties = new CacheProperties(60, 60, 60, 60, 1000);
        CacheManager manager = new CacheConfig().cacheManager(properties);

        assertThat(manager.getCacheNames()).containsExactlyInAnyOrder(
                CacheNames.BUSINESS_SETTINGS, CacheNames.BUSINESS_OPENING_HOURS,
                CacheNames.CATALOG_SERVICES, CacheNames.CATALOG_BUSINESS_SERVICES);
    }

    @Test
    void deveAplicarDefaultsQuandoValoresInvalidos() {
        CacheProperties properties = new CacheProperties(0, -1, 0, 0, 0);

        assertThat(properties.businessSettingsTtlSeconds()).isEqualTo(600);
        assertThat(properties.businessOpeningHoursTtlSeconds()).isEqualTo(600);
        assertThat(properties.catalogServicesTtlSeconds()).isEqualTo(120);
        assertThat(properties.defaultMaxEntries()).isEqualTo(10_000);
    }
}