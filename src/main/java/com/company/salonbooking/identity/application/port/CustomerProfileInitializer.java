package com.company.salonbooking.identity.application.port;

import java.util.UUID;

/** Called right after a CUSTOMER User is registered, so a CustomerProfile always exists (Seção 18). */
public interface CustomerProfileInitializer {

    void initializeFor(UUID userId);
}