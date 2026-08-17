package com.company.salonbooking.infrastructure.security;

import com.company.salonbooking.identity.domain.model.Role;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedUser(UUID userId, String email, Set<Role> roles, UUID businessId) {

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }
}
