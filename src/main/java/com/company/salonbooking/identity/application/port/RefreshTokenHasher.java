package com.company.salonbooking.identity.application.port;

public interface RefreshTokenHasher {
    String hash(String rawToken);
    boolean matches(String rawToken, String hashedToken);
}