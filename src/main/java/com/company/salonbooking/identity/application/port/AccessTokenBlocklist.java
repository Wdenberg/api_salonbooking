package com.company.salonbooking.identity.application.port;

import java.time.Instant;

public interface AccessTokenBlocklist {
    void add(String accessToken, Instant now);
    boolean isBlocked(String accessToken);
}