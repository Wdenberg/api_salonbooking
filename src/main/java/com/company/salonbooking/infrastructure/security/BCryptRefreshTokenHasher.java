package com.company.salonbooking.infrastructure.security;

import com.company.salonbooking.identity.application.port.RefreshTokenHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptRefreshTokenHasher implements RefreshTokenHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String rawToken) {
        return encoder.encode(rawToken);
    }

    @Override
    public boolean matches(String rawToken, String hashedToken) {
        return encoder.matches(rawToken, hashedToken);
    }
}