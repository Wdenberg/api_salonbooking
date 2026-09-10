package com.company.salonbooking.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * Reads the current HTTP request's IP and User-Agent, when one exists. Returns empty
 * outside a web request context (e.g. called from a @Scheduled job or a RabbitMQ
 * consumer thread), so AuditRecorder can safely omit these fields there instead of
 * failing (Seção 41 does not require ipAddress/userAgent for every audited action —
 * they're explicitly marked optional in the AuditEvent schema).
 */
@Component
public class RequestMetadataHolder {

    private static final int MAX_IP_LENGTH = 45;
    private static final int MAX_USER_AGENT_LENGTH = 500;

    public Optional<String> currentIpAddress() {
        return currentRequest().map(this::extractClientIp);
    }

    public Optional<String> currentUserAgent() {
        return currentRequest()
                .map(request -> request.getHeader("User-Agent"))
                .map(ua -> truncate(ua, MAX_USER_AGENT_LENGTH));
    }

    private Optional<HttpServletRequest> currentRequest() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            return Optional.of(servletAttributes.getRequest());
        }
        return Optional.empty();
    }

    private String extractClientIp(HttpServletRequest request) {
        // X-Forwarded-For is trusted here on the assumption of a reverse proxy in front
        // of the application (typical deployment per Seção 78/79); takes the first hop.
        String forwardedFor = request.getHeader("X-Forwarded-For");
        String ip = (forwardedFor != null && !forwardedFor.isBlank())
                ? forwardedFor.split(",")[0].trim()
                : request.getRemoteAddr();
        return truncate(ip, MAX_IP_LENGTH);
    }

    private String truncate(String value, int maxLength) {
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
