package com.company.salonbooking.identity.application.port;

import com.company.salonbooking.identity.domain.model.User;

import java.util.Optional;
import java.util.UUID;

/** Resolves the tenant (businessId) an authenticated EMPLOYEE belongs to, for embedding in the JWT (Seção 124). */
public interface BusinessContextResolver {

    Optional<UUID> resolveBusinessId(User user);
}