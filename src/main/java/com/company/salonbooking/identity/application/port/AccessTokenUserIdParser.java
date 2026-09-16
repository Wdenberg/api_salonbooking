package com.company.salonbooking.identity.application.port;

import java.util.Optional;
import java.util.UUID;

public interface AccessTokenUserIdParser {
    Optional<UUID> extractUserId(String accessToken);
}
