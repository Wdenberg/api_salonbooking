package com.company.salonbooking.infrastructure.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (!properties.enabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        String clientIp = resolveClientIp(request);

        String bucketKey = buildBucketKey(clientIp, path);

        RateLimitProperties.EndpointLimit endpointLimit =
                resolveEndpointLimit(path);

        Bucket bucket = buckets.computeIfAbsent(
                bucketKey,
                key -> createBucket(endpointLimit)
        );

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            addRateLimitHeaders(
                    response,
                    endpointLimit,
                    probe.getRemainingTokens()
            );

            filterChain.doFilter(request, response);
            return;
        }

        handleRateLimitExceeded(
                response,
                path,
                endpointLimit,
                probe
        );
    }

    private RateLimitProperties.EndpointLimit resolveEndpointLimit(
            String path
    ) {

        RateLimitProperties.EndpointLimit configuredLimit =
                properties.endpoints().get(path);

        if (configuredLimit != null) {
            return configuredLimit;
        }

        return new RateLimitProperties.EndpointLimit(
                properties.defaultLimit().requestsPerMinute(),
                properties.defaultLimit().requestsPerSecond()
        );
    }

    private Bucket createBucket(
            RateLimitProperties.EndpointLimit limit
    ) {

        Bandwidth minuteLimit = Bandwidth.classic(
                limit.requestsPerMinute(),
                Refill.intervally(
                        limit.requestsPerMinute(),
                        Duration.ofMinutes(1)
                )
        );

        Bandwidth secondLimit = Bandwidth.classic(
                limit.requestsPerSecond(),
                Refill.intervally(
                        limit.requestsPerSecond(),
                        Duration.ofSeconds(1)
                )
        );

        return Bucket.builder()
                .addLimit(minuteLimit)
                .addLimit(secondLimit)
                .build();
    }

    private void addRateLimitHeaders(
            HttpServletResponse response,
            RateLimitProperties.EndpointLimit limit,
            long remainingTokens
    ) {

        response.setHeader(
                "X-RateLimit-Limit-Minute",
                String.valueOf(limit.requestsPerMinute())
        );

        response.setHeader(
                "X-RateLimit-Limit-Second",
                String.valueOf(limit.requestsPerSecond())
        );

        /*
         * Bucket4j exposes the remaining tokens of the bucket as a whole.
         *
         * Because this bucket contains two bandwidth limits
         * (minute + second), the exact remaining value for each individual
         * bandwidth cannot be derived from getAvailableTokens().
         *
         * Therefore we expose the conservative remaining value.
         */
        response.setHeader(
                "X-RateLimit-Remaining",
                String.valueOf(Math.max(0, remainingTokens))
        );
    }

    private void handleRateLimitExceeded(
            HttpServletResponse response,
            String path,
            RateLimitProperties.EndpointLimit limit,
            ConsumptionProbe probe
    ) throws IOException {

        long retryAfterSeconds = calculateRetryAfterSeconds(probe);

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        addRateLimitHeaders(
                response,
                limit,
                Math.max(0, probe.getRemainingTokens())
        );

        response.setHeader(
                "Retry-After",
                String.valueOf(retryAfterSeconds)
        );

        String body = """
                {
                  "timestamp": "%s",
                  "status": 429,
                  "code": "RATE_LIMIT_EXCEEDED",
                  "message": "Rate limit exceeded. Try again in %d seconds.",
                  "path": "%s"
                }
                """.formatted(
                Instant.now(),
                retryAfterSeconds,
                path
        );

        response.getWriter().write(body);
    }

    private long calculateRetryAfterSeconds(
            ConsumptionProbe probe
    ) {

        long nanos = probe.getNanosToWaitForRefill();

        return Math.max(
                1,
                (nanos + 999_999_999L) / 1_000_000_000L
        );
    }

    private String buildBucketKey(
            String clientIp,
            String path
    ) {

        return clientIp + ":" + path;
    }

    private String resolveClientIp(
            HttpServletRequest request
    ) {

        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor
                    .split(",")[0]
                    .trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");

        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}

