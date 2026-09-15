package com.company.salonbooking.identity.application.port;

import com.company.salonbooking.identity.domain.model.User;

public interface TokenIssuer {

    IssuedToken issueToken(User user);

    record IssuedToken(String accessToken, String refreshToken, long accessTokenExpiresInSeconds, long refreshTokenExpiresInSeconds) {}
}
