package com.company.salonbooking.identity.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


public final class User {

    private final UUID id;
    private String name;
    private final String email;
    private String passwordHash;
    private UserStatus status;
    private final Set<Role> roles;
    private final Instant createdAt;
    private Instant updatedAt;

    private User(UUID id, String name, String email, String passwordHash, UserStatus status,
                 Set<Role> roles, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = requireNonBlank(name, "name");
        this.email = requireNonBlank(email, "email").toLowerCase();
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.roles = EnumSet.copyOf(roles.isEmpty() ? EnumSet.noneOf(Role.class) : roles);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    /** Registers a brand-new user with a single initial role. Role is never client-supplied. */
    public static User register(UUID id, String name, String email, String passwordHash, Role role, Instant now) {
        return new User(id, name, email, passwordHash, UserStatus.ACTIVE, EnumSet.of(role), now, now);
    }

    /** Reconstructs a User from persisted state. Used only by infrastructure mappers. */
    public static User restore(UUID id, String name, String email, String passwordHash, UserStatus status,
                               Set<Role> roles, Instant createdAt, Instant updatedAt) {
        return new User(id, name, email, passwordHash, status, roles, createdAt, updatedAt);
    }

    public void block(Instant now) {
        this.status = UserStatus.BLOCKED;
        this.updatedAt = now;
    }

    public void activate(Instant now) {
        this.status = UserStatus.ACTIVE;
        this.updatedAt = now;
    }

    public void deactivate(Instant now) {
        this.status = UserStatus.INACTIVE;
        this.updatedAt = now;
    }

    public void rename(String newName, Instant now) {
        this.name = requireNonBlank(newName, "name");
        this.updatedAt = now;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public UserStatus getStatus() { return status; }
    public Set<Role> getRoles() { return Collections.unmodifiableSet(roles); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}