package com.company.salonbooking.infrastructure.security;

import com.company.salonbooking.identity.application.port.TokenIssuer;
import com.company.salonbooking.identity.domain.model.Role;
import com.company.salonbooking.identity.domain.model.User;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtService implements TokenIssuer {

    private static final String ROLES_CLAIM = "roles";
    private static final int MIN_SECRET_BYTES = 32; // HS256 requires >= 256 bits

    private final SecretKey key;
    private final long expirationSeconds;
    private final Clock clock;

    public JwtService(JwtProperties properties, Clock clock) {
        if (properties.secret() == null || properties.secret().getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "app.jwt.secret must be configured with at least 32 bytes. " +
                            "Set the JWT_SECRET environment variable.");
        }
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = properties.expirationSeconds();
        this.clock = clock;
    }

    @Override
    public IssuedToken issueToken(User user) {
        Instant now = Instant.now(clock);
        Instant expiry = now.plusSeconds(expirationSeconds);

        List<String> roleNames = user.getRoles().stream().map(Enum::name).collect(Collectors.toList());

        String token = Jwts.builder()
                .subject(user.getId().toString())
                .claim(ROLES_CLAIM, roleNames)
                .claim("email", user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();

        return new IssuedToken(token, expirationSeconds);
    }

    /** Returns empty if the token is missing, malformed, expired, or has an invalid signature. */
    public java.util.Optional<AuthenticatedUser> parse(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

            UUID userId = UUID.fromString(claims.getSubject());
            String email = claims.get("email", String.class);

            @SuppressWarnings("unchecked")
            List<String> roleNames = claims.get(ROLES_CLAIM, List.class);
            Set<Role> roles = roleNames == null
                    ? Set.of()
                    : roleNames.stream().map(Role::valueOf).collect(Collectors.toSet());

            // businessId is not yet part of the token in this phase; added when
            // the business/employee modules exist and re-issue tokens with it.
            return java.util.Optional.of(new AuthenticatedUser(userId, email, roles, null));
        } catch (JwtException | IllegalArgumentException e) {
            return java.util.Optional.empty();
        }
    }
}