package com.company.salonbooking.identity.domain.exception;

public class RefreshTokenReuseDetectedException extends RuntimeException {
    public RefreshTokenReuseDetectedException() {
        super("Refresh token reuse detected - possible token theft");
    }
}