package com.company.salonbooking.infrastructure.security;

import com.company.salonbooking.identity.application.port.BusinessContextResolver;
import com.company.salonbooking.identity.application.port.TokenIssuer;
import com.company.salonbooking.identity.domain.model.Role;
import com.company.salonbooking.identity.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtService implements TokenIssuer {

    private static final String ROLES_CLAIM = "roles";
    private static final String BUSINESS_ID_CLAIM = "businessId";
    private static final int MIN_SECRET_BYTES = 32;

    private final SecretKey key;
    private final long expirationSeconds;
    private final Clock clock;
    private final BusinessContextResolver businessContextResolver;

    public JwtService(JwtProperties properties, Clock clock, BusinessContextResolver businessContextResolver) {
        if (properties.secret() == null || properties.secret().getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "app.jwt.secret must be configured with at least 32 bytes. Set the JWT_SECRET environment variable.");
        }
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = properties.expirationSeconds();
        this.clock = clock;
        this.businessContextResolver = businessContextResolver;
    }

    @Override
    public IssuedToken issueToken(User user) {
        Instant now = Instant.now(clock);
        Instant expiry = now.plusSeconds(expirationSeconds);

        List<String> roleNames = user.getRoles().stream().map(Enum::name).collect(Collectors.toList());

        var builder = Jwts.builder()
                .subject(user.getId().toString())
                .claim(ROLES_CLAIM, roleNames)
                .claim("email", user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry));

        businessContextResolver.resolveBusinessId(user)
                .ifPresent(businessId -> builder.claim(BUSINESS_ID_CLAIM, businessId.toString()));

        String token = builder.signWith(key).compact();
        return new IssuedToken(token, expirationSeconds);
    }

    public Optional<AuthenticatedUser> parse(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

            UUID userId = UUID.fromString(claims.getSubject());
            String email = claims.get("email", String.class);

            @SuppressWarnings("unchecked")
            List<String> roleNames = claims.get(ROLES_CLAIM, List.class);
            Set<Role> roles = roleNames == null
                    ? Set.of()
                    : roleNames.stream().map(Role::valueOf).collect(Collectors.toSet());

            String businessIdClaim = claims.get(BUSINESS_ID_CLAIM, String.class);
            UUID businessId = businessIdClaim == null ? null : UUID.fromString(businessIdClaim);

            return Optional.of(new AuthenticatedUser(userId, email, roles, businessId));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}