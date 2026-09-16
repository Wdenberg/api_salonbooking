package com.company.salonbooking.identity.domain.repository;

import com.company.salonbooking.identity.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    User save(User user);

    /** Cross-tenant: finds all users across all businesses (PLATFORM_ADMIN only). */
    List<User> findAll(int page, int size);
}
