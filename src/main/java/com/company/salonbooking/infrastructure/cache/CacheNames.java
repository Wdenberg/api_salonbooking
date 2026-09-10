package com.company.salonbooking.infrastructure.cache;

/** Centralizes cache names so publisher (use cases) and configuration (TTL policy) never drift apart. */
public final class CacheNames {

    private CacheNames() {
    }

    public static final String BUSINESS_SETTINGS = "business-settings";
    public static final String BUSINESS_OPENING_HOURS = "business-opening-hours";
    public static final String CATALOG_SERVICES = "catalog-services";
    public static final String CATALOG_BUSINESS_SERVICES = "catalog-business-services";
}
