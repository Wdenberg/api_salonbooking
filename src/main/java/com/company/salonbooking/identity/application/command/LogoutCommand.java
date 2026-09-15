package com.company.salonbooking.identity.application.command;

public record LogoutCommand(String accessToken, String refreshToken) {}