package com.company.salonbooking.identity.application.port;

import com.company.salonbooking.identity.domain.model.User;

public interface TokenIssuer {

    IssuedToken issueToken(User user);
    record  IssuedToken(String accessToken, long expiresInSeconds){}
}
