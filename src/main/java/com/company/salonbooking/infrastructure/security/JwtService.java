package com.company.salonbooking.infrastructure.security;

import com.company.salonbooking.identity.application.port.AccessTokenUserIdParser;
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
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtService implements TokenIssuer, AccessTokenUserIdParser {

    private static final String ROLES_CLAIM = "roles";
    private static final String BUSINESS_ID_CLAIM = "businessId";
    private static final int MIN_SECRET_BYTES = 32;
    private static final int REFRESH_TOKEN_BYTES = 32;

    private final SecretKey key;
    private final long accessTokenExpirationSeconds;
    private final long refreshTokenExpirationSeconds;
    private final Clock clock;
    private final BusinessContextResolver businessContextResolver;
    private final SecureRandom secureRandom;

    public JwtService(JwtProperties properties, Clock clock, BusinessContextResolver businessContextResolver) {
        this.key = createKey(properties);
        this.accessTokenExpirationSeconds = properties.expirationSeconds();
        this.refreshTokenExpirationSeconds = properties.refreshExpirationDays() * 24 * 60 * 60;
        this.clock = clock;
        this.businessContextResolver = businessContextResolver;
        this.secureRandom = new SecureRandom();
    }

    private static SecretKey createKey(JwtProperties properties) {
        String secret = properties.secret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "app.jwt.secret must be configured with at least 32 bytes. "
                            + "Set the JWT_SECRET environment variable.");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public IssuedToken issueToken(User user) {
        Instant now = Instant.now(clock);
        Instant accessExpiry = now.plusSeconds(accessTokenExpirationSeconds);
        Instant refreshExpiry = now.plusSeconds(refreshTokenExpirationSeconds);

        List<String> roleNames = user.getRoles().stream().map(Enum::name).collect(Collectors.toList());
        String jti = UUID.randomUUID().toString();

        var accessBuilder = Jwts.builder()
                .subject(user.getId().toString())
                .claim(ROLES_CLAIM, roleNames)
                .claim("email", user.getEmail())
                .id(jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(accessExpiry));

        businessContextResolver.resolveBusinessId(user)
                .ifPresent(businessId -> accessBuilder.claim(BUSINESS_ID_CLAIM, businessId.toString()));

        String accessToken = accessBuilder.signWith(key).compact();

        // Generate cryptographically secure refresh token (opaque, not JWT)
        String refreshToken = generateRefreshToken();

        return new IssuedToken(accessToken, refreshToken, accessTokenExpirationSeconds, refreshTokenExpirationSeconds);
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
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

    @Override
    public Optional<UUID> extractUserId(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return Optional.empty();
        }
        return parse(accessToken).map(AuthenticatedUser::userId);
    }

    public String extractJti(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return claims.getId();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public Instant extractExpiration(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return claims.getExpiration().toInstant();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}