package com.company.salonbooking.identity.infrastructure.persistence;

import com.company.salonbooking.identity.application.port.AccessTokenBlocklist;
import com.company.salonbooking.infrastructure.security.JwtService;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AccessTokenBlocklistAdapter implements AccessTokenBlocklist {

    private final AccessTokenBlocklistJpaRepository jpaRepository;
    private final JwtService jwtService;

    public AccessTokenBlocklistAdapter(AccessTokenBlocklistJpaRepository jpaRepository, JwtService jwtService) {
        this.jpaRepository = jpaRepository;
        this.jwtService = jwtService;
    }

    @Override
    public void add(String accessToken, Instant now) {
        String jti = jwtService.extractJti(accessToken);
        Instant expiry = jwtService.extractExpiration(accessToken);

        if (jti != null && expiry != null && expiry.isAfter(now)) {
            if (!jpaRepository.existsByTokenJti(jti)) {
                AccessTokenBlocklistJpaEntity entity = new AccessTokenBlocklistJpaEntity();
                entity.setId(java.util.UUID.randomUUID());
                entity.setTokenJti(jti);
                entity.setExpiresAt(expiry);
                entity.setCreatedAt(now);
                jpaRepository.save(entity);
            }
        }
    }

    @Override
    public boolean isBlocked(String accessToken) {
        String jti = jwtService.extractJti(accessToken);
        if (jti == null) {
            return false;
        }
        return jpaRepository.existsByTokenJti(jti);
    }
}